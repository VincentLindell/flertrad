#include <pthread.h>
#include <limits.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include "intset.h"

/*
 * Set implemented using closed hashing.
 */

// special value indicating a free array element
#define EMPTY_SLOT (INT_MIN)

// ----------------------------------------------------------------------------
// Local helper functions (internal use only)
static int index_for(struct intset *s, int a) {
    return abs(a % s->allocated);
}

static int find(struct intset *s, int a) {
    int idx = index_for(s, a);
    for (int i = 0; i < s->allocated; i++) {
        if (s->data[idx] == a || s->data[idx] == EMPTY_SLOT) {
            return idx;
        }
        idx = (idx + 1) % s->allocated;
    }
    return -1; // Should never happen
}

// ----------------------------------------------------------------------------

struct intset *intset_create() {
    struct intset *s = malloc(sizeof(struct intset));

    if (s == NULL) {
        perror("malloc");
        exit(1);
    }

    s->size = 0;
    s->allocated = 10;
    s->data = malloc(sizeof(int) * s->allocated);
    if (s->data == NULL) {
        perror("malloc");
        exit(1);
    }

    for (int i = 0; i < s->allocated; i++) {
        s->data[i] = EMPTY_SLOT;
    }

    // 🔒 initiera mutex
    pthread_mutex_init(&s->lock, NULL);

    return s;
}

// ----------------------------------------------------------------------------

bool intset_add(struct intset *s, int a) {
    pthread_mutex_lock(&s->lock); // 🔒 skydda mot parallella writes

    // rehash if more than 70% is used
    if (s->size >= s->allocated * 7 / 10) {
        int old_allocated = s->allocated;
        int *old_data = s->data;

        // double array size
        s->allocated *= 2;
        s->data = malloc(sizeof(int) * s->allocated);
        if (s->data == NULL) {
            perror("malloc");
            pthread_mutex_unlock(&s->lock);
            exit(1);
        }

        for (int i = 0; i < s->allocated; i++) {
            s->data[i] = EMPTY_SLOT;
        }

        // copy values to new array
        for (int i = 0; i < old_allocated; i++) {
            int val = old_data[i];
            if (val != EMPTY_SLOT) {
                int idx = index_for(s, val);
                int attempts = 0;
                while (s->data[idx] != EMPTY_SLOT && attempts < s->allocated) {
                    idx = (idx + 1) % s->allocated;
                    attempts++;
                }
                s->data[idx] = val;
            }
        }
        free(old_data);
    }

    int idx = find(s, a);
    if (s->data[idx] == a) {
        pthread_mutex_unlock(&s->lock); // 🔓 före return
        return false;
    }

    s->data[idx] = a;
    s->size++;

    pthread_mutex_unlock(&s->lock); // 🔓
    return true;
}

// ----------------------------------------------------------------------------

bool intset_contains(struct intset *s, int a) {
    pthread_mutex_lock(&s->lock); // 🔒 skydda läsning
    int idx = find(s, a);
    bool found = (s->data[idx] == a);
    pthread_mutex_unlock(&s->lock); // 🔓
    return found;
}

// ----------------------------------------------------------------------------

int intset_size(struct intset *s) {
    pthread_mutex_lock(&s->lock);
    int sz = s->size;
    pthread_mutex_unlock(&s->lock);
    return sz;
}
