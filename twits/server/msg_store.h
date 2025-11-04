//  -------------------------------------------------------------------------
//
//  msg_store:
//
//  Functions for storing and retrieving topics and messages. Every
//  message belongs to a particular topic.
//
//  Each client subscribes to one topic at a time. New topics are
//  always published to all clients. New messages are published to all
//  clients that subscribe to the topic the message belongs to.
//
//  -------------------------------------------------------------------------

#ifndef MSG_STORE_H
#define MSG_STORE_H

#include <stdbool.h>
#include <pthread.h>

struct msg_store;
struct client_state; // defined below

/**
 * Create a new message store.
 */
struct msg_store * msg_store_create();

/**
 * Adds a new topic to the message store.
 */
int msg_store_add_topic(struct msg_store *store,
                        char *username,
                        char *text);

/**
 * Adds a new message to the topic currently subscribed to by _client_.
 */
void msg_store_add_message(struct msg_store *store,
                           struct client_state *client,
                           char *username,
                           char *text);

/**
 * Retrieves the next topic that needs to be published to this client.
 */
bool msg_store_check_for_new_topic(struct msg_store *store,
                                   struct client_state *client,
                                   int *topic_id,
                                   char **username,
                                   char **text);

/**
 * Retrieves the next message (within the current topic) that needs to
 * be published to this client.
 */
bool msg_store_check_for_new_message(struct msg_store *store,
                                     struct client_state *client,
                                     int *topic_id,
                                     int *message_id,
                                     char **username,
                                     char **text);

/**
 * Checks for a topic that has new messages (that is, an increased message
 * count).
 */
bool msg_store_check_for_updated_message_count(struct msg_store *store,
                                               struct client_state *client,
                                               int *topic_id,
                                               int *message_count);

/**
 * Waits until either a new message or a new topic is available for _client_.
 */
int msg_store_await_message_or_topic(struct msg_store *store,
                                     struct client_state *client);

/**
 * Initialize client_state for a particular client.
 */
void msg_store_init_client(struct msg_store *store,
                           struct client_state *client);

/**
 * Dispose of client_state for a particular client.
 */
void msg_store_dispose_client(struct msg_store *store,
                              struct client_state *client);

/**
 * Set _client_ to subscribe to topic _topic_id_.
 */
void msg_store_select_topic(struct msg_store *store,
                            struct client_state *client,
                            int topic_id);

// ----------------------------------------------------------------------------

/**
 * Struct for keeping track of what has been reported to a particular client.
 */
struct client_state {
  int current_topic_id;          // topic selected by this client
  int nbr_read;                  // number of messages read (in current topic)
  struct list *message_counts;   // number of reported messages for each topic
};

/** Special values for 'current_topic_id', to denote client state */
enum {
  TOPIC_STATE_NO_TOPIC         = -1,
  TOPIC_STATE_LOGOUT_REQUESTED = -2,
  TOPIC_STATE_DISCONNECTED     = -3
};

// ----------------------------------------------------------------------------
// Add synchronization primitives for thread safety
// ----------------------------------------------------------------------------

#endif
