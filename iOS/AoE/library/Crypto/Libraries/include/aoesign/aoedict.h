//
//  aoedict.h
//  aoesign
//
//  Created by dingchao on 2019/10/23.
//  Copyright © 2019 dingchao. All rights reserved.
//

#ifndef aoedict_h
#define aoedict_h

#define DICT_OK 0
#define DICT_ERR 1

/* Unused arguments generate annoying warnings... */
//#define DICT_NOTUSED(V) ((void) V)

typedef struct aoe_dictEntry {
    void *key;
    void *val;
    struct aoe_dictEntry *next;
} aoe_dictEntry;

typedef struct aoe_dictType {
    unsigned int (*hashFunction)(const void *key);
    void *(*keyDup)(void *privdata, const void *key);
    void *(*valDup)(void *privdata, const void *obj);
    int (*keyCompare)(void *privdata, const void *key1, const void *key2);
    void (*keyDestructor)(void *privdata, void *key);
    void (*valDestructor)(void *privdata, void *obj);
} aoe_dictType;

typedef struct aoe_dict {
    aoe_dictEntry **table;
    aoe_dictType *type;
    unsigned long size;
    unsigned long sizemask;
    unsigned long used;
    void *privdata;
} aoe_dict;

typedef struct aoe_dictIterator {
    aoe_dict *ht;
    int index;
    aoe_dictEntry *entry, *nextEntry;
} aoe_dictIterator;

/* This is the initial size of every hash table */
#define DICT_HT_INITIAL_SIZE     4

/* ------------------------------- Macros ------------------------------------*/
#define dictFreeEntryVal(ht, entry) \
    if ((ht)->type->valDestructor) \
        (ht)->type->valDestructor((ht)->privdata, (entry)->val)

#define dictSetHashVal(ht, entry, _val_) do { \
    if ((ht)->type->valDup) \
        entry->val = (ht)->type->valDup((ht)->privdata, _val_); \
    else \
        entry->val = (_val_); \
} while(0)

#define dictFreeEntryKey(ht, entry) \
    if ((ht)->type->keyDestructor) \
        (ht)->type->keyDestructor((ht)->privdata, (entry)->key)

#define dictSetHashKey(ht, entry, _key_) do { \
    if ((ht)->type->keyDup) \
        entry->key = (ht)->type->keyDup((ht)->privdata, _key_); \
    else \
        entry->key = (_key_); \
} while(0)

#define dictCompareHashKeys(ht, key1, key2) \
    (((ht)->type->keyCompare) ? \
        (ht)->type->keyCompare((ht)->privdata, key1, key2) : \
        (key1) == (key2))

#define dictHashKey(ht, key) (ht)->type->hashFunction(key)

#define dictGetEntryKey(he) ((he)->key)
#define dictGetEntryVal(he) ((he)->val)
#define dictSlots(ht) ((ht)->size)
#define dictSize(ht) ((ht)->used)

/* API */
unsigned int aoe_dictGenHashFunction(const unsigned char *buf, int len);
 aoe_dict *aoe_dictCreate(aoe_dictType *type, void *privDataPtr);
 int aoe_dictExpand(aoe_dict *ht, unsigned long size);
 int aoe_dictAdd(aoe_dict *ht, void *key, void *val);
 int aoe_dictReplace(aoe_dict *ht, void *key, void *val);
 int aoe_dictDelete(aoe_dict *ht, const void *key);
 void aoe_dictRelease(aoe_dict *ht);
 aoe_dictEntry * aoe_dictFind(aoe_dict *ht, const void *key);
 aoe_dictIterator *aoe_dictGetIterator(aoe_dict *ht);
 aoe_dictEntry *aoe_dictNext(aoe_dictIterator *iter);
 void aoe_dictReleaseIterator(aoe_dictIterator *iter);

#endif /* aoedict_h */
