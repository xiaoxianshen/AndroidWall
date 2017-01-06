/*
* Copyright (C) 2014 MediaTek Inc.
* Modification based on code covered by the mentioned copyright
* and/or permission notice(s).
*/
/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <cutils/properties.h>
#include <arpa/inet.h>

#define LOG_TAG "FirewallController"
#define LOG_NDEBUG 0

#include <cutils/log.h>
#include <private/android_filesystem_config.h>

#include "NetdConstants.h"
#include "FirewallController.h"
//add by xiaobin01
#include <cutils/properties.h>  
  
const char* op_3g;  
const char* op_wifi; 
//add by xiaobin01
const char* FirewallController::TABLE = "filter";

const char* FirewallController::LOCAL_INPUT = "fw_INPUT";
const char* FirewallController::LOCAL_OUTPUT = "fw_OUTPUT";
const char* FirewallController::LOCAL_FORWARD = "fw_FORWARD";

// use bw chain for sanity issue. need create fw chain later
const char* FirewallController::LOCAL_MANGLE_POSTROUTING = "fw_mangle_POSTROUTING";

// Support tencent firewall @{
const char* FirewallController::FIREWALL = "firewall";
const char* FirewallController::FIREWALL_MOBILE = "mobile";
const char* FirewallController::FIREWALL_WIFI = "wifi";
//@}

// for ALPS01918633
bool openNsiotFlag = false;
bool openNsiotVolteFlag = false;
const char* FirewallController::IPT_PATH = "/data/misc/dhcp/fw_iptables.conf";

const char* FirewallController::LOCAL_DOZABLE = "fw_dozable";
const char* FirewallController::LOCAL_STANDBY = "fw_standby";

// ICMPv6 types that are required for any form of IPv6 connectivity to work. Note that because the
// fw_dozable chain is called from both INPUT and OUTPUT, this includes both packets that we need
// to be able to send (e.g., RS, NS), and packets that we need to receive (e.g., RA, NA).
const char* FirewallController::ICMPV6_TYPES[] = {
    "packet-too-big",
    "router-solicitation",
    "router-advertisement",
    "neighbour-solicitation",
    "neighbour-advertisement",
    "redirect",
};

FirewallController::FirewallController(void) {
    // If no rules are set, it's in BLACKLIST mode
    mFirewallType = BLACKLIST;
}

int FirewallController::setupIptablesHooks(void) {

    /*********************************************************************
	* mtk03594: Support tencent firewall
	* mount child chain,FIREWALL_MOBILE and FIREWALL_WIFI, to FIREWALL
	*********************************************************************/
    int res = 0;
    res |= execIptables(V4V6, "-F", FIREWALL, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "ppp+", "-j", FIREWALL_MOBILE, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "ccmni+", "-j", FIREWALL_MOBILE, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "ccemni+", "-j", FIREWALL_MOBILE, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "usb+", "-j", FIREWALL_MOBILE, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "cc2mni+", "-j", FIREWALL_MOBILE, NULL);
    res |= execIptables(V4V6, "-A", FIREWALL, "-o", "wlan+", "-j", FIREWALL_WIFI, NULL);

    // child chains are created but not attached, they will be attached explicitly.
    FirewallType firewallType = getFirewallType(DOZABLE);
    res |= createChain(LOCAL_DOZABLE, LOCAL_INPUT, firewallType);

    firewallType = getFirewallType(STANDBY);
    res |= createChain(LOCAL_STANDBY, LOCAL_INPUT, firewallType);

    return res;
}

int FirewallController::enableFirewall(FirewallType ftype) {
    int res = 0;
    if (mFirewallType != ftype) {
        // flush any existing rules
        disableFirewall();


    if (ftype == WHITELIST) {
    // create default rule to drop all traffic
    res |= execIptables(V4V6, "-A", LOCAL_INPUT, "-j", "DROP", NULL);
    //res |= execIptables(V4V6, "-A", LOCAL_OUTPUT, "-j", "REJECT", NULL);
    res |= execIptables(V4V6, "-A", LOCAL_FORWARD, "-j", "REJECT", NULL);
    res |= execIptables(V4V6, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-j", "DROP", NULL);

      }
        // Set this after calling disableFirewall(), since it defaults to WHITELIST there
        mFirewallType = ftype;
    }
    return res;
}

int FirewallController::disableFirewall(void) {
    int res = 0;

    mFirewallType = WHITELIST;

    // flush any existing rules
    res |= execIptables(V4V6, "-F", LOCAL_INPUT, NULL);
    res |= execIptables(V4V6, "-F", LOCAL_OUTPUT, NULL);
    res |= execIptables(V4V6, "-F", LOCAL_FORWARD, NULL);
    res |= execIptables(V4V6, "-t", "mangle", "-F", LOCAL_MANGLE_POSTROUTING, NULL);

    return res;
}

int FirewallController::enableChildChains(ChildChain chain, bool enable) {
    int res = 0;
    const char* name;
    switch(chain) {
        case DOZABLE:
            name = LOCAL_DOZABLE;
            break;
        case STANDBY:
            name = LOCAL_STANDBY;
            break;
        default:
            return res;
    }

    if (enable) {
        res |= attachChain(name, LOCAL_INPUT);
        res |= attachChain(name, LOCAL_OUTPUT);
    } else {
        res |= detachChain(name, LOCAL_INPUT);
        res |= detachChain(name, LOCAL_OUTPUT);
    }
    return res;
}

int FirewallController::isFirewallEnabled(void) {
    // TODO: verify that rules are still in place near top
    return -1;
}

int FirewallController::setInterfaceRule(const char* iface, FirewallRule rule) {
    if (mFirewallType == BLACKLIST) {
        // Unsupported in BLACKLIST mode
        return -1;
    }

    if (!isIfaceName(iface)) {
        errno = ENOENT;
        return -1;
    }

    const char* op;
    if (rule == ALLOW) {
        op = "-I";
    } else {
        op = "-D";
    }

    int res = 0;
    res |= execIptables(V4V6, op, LOCAL_INPUT, "-i", iface, "-j", "RETURN", NULL);
    res |= execIptables(V4V6, op, LOCAL_OUTPUT, "-o", iface, "-j", "RETURN", NULL);
    res |= execIptables(V4V6, "-t", "mangle", op, LOCAL_MANGLE_POSTROUTING, "-o", iface, "-j", "RETURN", NULL);
    return res;
}

int FirewallController::setEgressSourceRule(const char* addr, FirewallRule rule) {
    if (mFirewallType == BLACKLIST) {
        // Unsupported in BLACKLIST mode
        return -1;
    }

    IptablesTarget target = V4;
    if (strchr(addr, ':')) {
        target = V6;
    }

    const char* op;
    if (rule == ALLOW) {
        op = "-I";
    } else {
        op = "-D";
    }

    int res = 0;
    res |= execIptables(target, op, LOCAL_INPUT, "-d", addr, "-j", "RETURN", NULL);
    res |= execIptables(target, op, LOCAL_OUTPUT, "-s", addr, "-j", "RETURN", NULL);
    res |= execIptables(target, "-t", "mangle", op, LOCAL_MANGLE_POSTROUTING, "-s", addr, "-j", "RETURN", NULL);
    return res;
}

int FirewallController::setEgressDestRule(const char* addr, int protocol, int port,
        FirewallRule rule) {
    if (mFirewallType == BLACKLIST) {
        // Unsupported in BLACKLIST mode
        return -1;
    }

    IptablesTarget target = V4;
    if (strchr(addr, ':')) {
        target = V6;
    }

    char protocolStr[16];
    sprintf(protocolStr, "%d", protocol);

    char portStr[16];
    sprintf(portStr, "%d", port);

    const char* op;
    if (rule == ALLOW) {
        op = "-I";
    } else {
        op = "-D";
    }

    int res = 0;
    res |= execIptables(target, op, LOCAL_INPUT, "-s", addr, "-p", protocolStr,
            "--sport", portStr, "-j", "RETURN", NULL);
    res |= execIptables(target, op, LOCAL_OUTPUT, "-d", addr, "-p", protocolStr,
            "--dport", portStr, "-j", "RETURN", NULL);
    res |= execIptables(target, "-t", "mangle", op, LOCAL_MANGLE_POSTROUTING, "-d", addr, "-p", protocolStr,
            "--dport", portStr, "-j", "RETURN", NULL);
    return res;
}

FirewallType FirewallController::getFirewallType(ChildChain chain) {
    switch(chain) {
        case DOZABLE:
            return WHITELIST;
        case STANDBY:
            return BLACKLIST;
        case NONE:
            return mFirewallType;
        default:
            return BLACKLIST;
    }
}

int FirewallController::setUidRule(ChildChain chain, int uid, FirewallRule rule) {
    char uidStr[16];
    sprintf(uidStr, "%d", uid);

    const char* op;
    const char* target;
    FirewallType firewallType = getFirewallType(chain);
    if (firewallType == WHITELIST) {
        target = "RETURN";
        op = (rule == ALLOW)? "-I" : "-D";
    } else { // BLACKLIST mode
        target = "DROP";
        op = (rule == DENY)? "-I" : "-D";
    }

    int res = 0;

    switch(chain) {
        case DOZABLE:
            res |= execIptables(V4V6, op, LOCAL_DOZABLE, "-m", "owner", "--uid-owner",
                    uidStr, "-j", target, NULL);
            break;
        case STANDBY:
            res |= execIptables(V4V6, op, LOCAL_STANDBY, "-m", "owner", "--uid-owner",
                    uidStr, "-j", target, NULL);
            break;
        case NONE:
            res |= execIptables(V4V6, op, LOCAL_INPUT, "-m", "owner", "--uid-owner", uidStr,
                    "-j", target, NULL);
            res |= execIptables(V4V6, op, LOCAL_OUTPUT, "-m", "owner", "--uid-owner", uidStr,
                    "-j", target, NULL);
            res |= execIptables(V4V6, "-t", "mangle", op, LOCAL_MANGLE_POSTROUTING, "-m", "owner", "--uid-owner", uidStr,
                    "-j", target, NULL);
            break;
        default:
            ALOGW("Unknown child chain: %d", chain);
            break;
    }

    return res;
}
//add by xiaobin01
int FirewallController::initIptableFirewall(void) {  
    int res = 0;  
    //char property[PROPERTY_VALUE_MAX];  
    //property_get("ro.hardware", property, "qcom");  
    //if (strncmp("qcom", property, 4) == 0) { // qcom  
     //   op_3g = "rmnet+";  
      //  op_wifi = "wlan0";  
    //} else if (strncmp("mt", property, 2) == 0 || strncmp("MT", property, 2) == 0) {  
        op_3g = "ccmni+"; // MTK  
        op_wifi = "wlan0";  
    //} else {  
    //    op_3g = "rmnet+";  
    //    op_wifi = "wlan0";  
    //}  
    res |= execIptables(V4V6, "-w", "-N", "drop_wall", NULL);  
    res |= execIptables(V4V6, "-w", "-A", "OUTPUT", "-j", "drop_wall", NULL);  
	return res;
}  
  
int FirewallController::setMobileDataUidRule(int uid, FirewallRule rule) {  
    char uidStr[16];  
    sprintf(uidStr, "%d", uid);  
    const char* op;  
    if (rule == ALLOW) {  
        op = "-D";// 
    } else {  
        op = "-A";// 
    }  
    int res = 0;  
    res |= execIptables(V4V6, "-w", op, "drop_wall", "-o", op_3g, "-m", "owner",  
            "--uid-owner", uidStr, "-j", "REJECT", NULL);  
    return res;  
}  
  
int FirewallController::setWifiDataUidRule(int uid, FirewallRule rule) {  
    char uidStr[16];  
    sprintf(uidStr, "%d", uid);  
    const char* op;  
    if (rule == ALLOW) {  
        op = "-D";//  
    } else {  
        op = "-A";//  
    }  
    int res = 0;  
    res |= execIptables(V4V6, "-w", op, "drop_wall", "-o", op_wifi, "-m", "owner",  
            "--uid-owner", uidStr, "-j", "REJECT", NULL);  
    return res;  
}  
//add by xiaobin01
int FirewallController::attachChain(const char* childChain, const char* parentChain) {
    return execIptables(V4V6, "-t", TABLE, "-A", parentChain, "-j", childChain, NULL);
}

int FirewallController::detachChain(const char* childChain, const char* parentChain) {
    return execIptables(V4V6, "-t", TABLE, "-D", parentChain, "-j", childChain, NULL);
}

int FirewallController::createChain(const char* childChain,
        const char* parentChain, FirewallType type) {
    // Order is important, otherwise later steps may fail.
    execIptablesSilently(V4V6, "-t", TABLE, "-D", parentChain, "-j", childChain, NULL);
    execIptablesSilently(V4V6, "-t", TABLE, "-F", childChain, NULL);
    execIptablesSilently(V4V6, "-t", TABLE, "-X", childChain, NULL);
    int res = 0;
    res |= execIptables(V4V6, "-t", TABLE, "-N", childChain, NULL);
    if (type == WHITELIST) {
        // Allow ICMPv6 packets necessary to make IPv6 connectivity work. http://b/23158230 .
        for (size_t i = 0; i < ARRAY_SIZE(ICMPV6_TYPES); i++) {
            res |= execIptables(V6, "-A", childChain, "-p", "icmpv6", "--icmpv6-type",
                    ICMPV6_TYPES[i], "-j", "RETURN", NULL);
        }

        // create default white list for system uid range
        char uidStr[16];
        sprintf(uidStr, "0-%d", AID_APP - 1);
        res |= execIptables(V4V6, "-A", childChain, "-m", "owner", "--uid-owner",
                uidStr, "-j", "RETURN", NULL);

        // create default rule to drop all traffic
        res |= execIptables(V4V6, "-A", childChain, "-j", "DROP", NULL);
    }

    return res;
}

/*MTK: support nsiot*/
int FirewallController::setUdpForwarding(const char* inInterface, const char* extInterface, const char* ipAddr) {
    struct in_addr s4;
    IptablesTarget target = V4;
    int res = 0;

    if(inInterface==NULL || extInterface==NULL || ipAddr==NULL){
        ALOGE("setUdpForwarding: invalid args");
        return -1;
    }
    if((0 != strncmp(extInterface, "cc", 2)) || (0 != strncmp(inInterface, "rndis", 5))) {
        ALOGE("setUdpForwarding: extInterface %s and inInterface %s is not permitted", extInterface, inInterface);
        return -1;
    }
    ALOGD("setUdpForwarding: %s-%s:%s", inInterface, extInterface, ipAddr);

    if (inet_pton(AF_INET, ipAddr, &s4) != 1){
        ALOGE("setUdpForwarding: invalid IPv4 address");
        return -1;
    }

    //Delete the old IPTABLE rule
    res |= execIptables(target, "-F", "oem_fwd", NULL);
    res |= execIptables(target, "-I", "oem_fwd", "-i", inInterface, "-o", extInterface, "-j", "ACCEPT", NULL);
    res |= execIptables(target, "-I", "oem_fwd", "-i", extInterface, "-o", inInterface, "-j", "ACCEPT", NULL);
    res |= execIptables(target, "-t", "nat", "-F", "PREROUTING", NULL);
    res |= execIptables(target, "-t", "nat", "-I", "PREROUTING", "-i", extInterface, "-j", "DNAT", "--to", ipAddr, NULL);

    return res;
}

/*MTK: support nsiot*/
int FirewallController::clearUdpForwarding(const char* inInterface, const char* extInterface) {
    IptablesTarget target = V4;
    int res = 0;

    if(inInterface==NULL || extInterface==NULL){
        ALOGW("clearUdpForwarding: invalid args");
    } else {
        ALOGD("clearUdpForwarding: %s-%s", inInterface, extInterface);
    }
    //Delete the old IPTABLE rule
    res |= execIptables(target, "-F", "oem_fwd", NULL);
    res |= execIptables(target, "-t", "nat", "-F", "PREROUTING", NULL);
    property_set("net.rndis.client", "");
    return res;

}

/*support nsiot*/
int FirewallController::setNsiotFirewall(void) {
    int res = 0;
    IptablesTarget target = V4;
    const char** allowed_ip = NSIOT_WHITE_LIST;

    //mkt07384: if nsiot is opened , do nothing
    if(openNsiotFlag){
        ALOGD("Nsiot already opened!");
         return 0;
      }
    //volte-nsiot open
     if(openNsiotVolteFlag){
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string", "spirent",
                        "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string", "slp.rs.de",
                         "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string",
                       "pub.3gppnetwork.org", "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(V4V6, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-j", "DROP", NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-d", "10.0.0.0/8", "-j", "ACCEPT", NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-d", "172.16.0.0/12", "-j", "ACCEPT", NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-d", "192.168.0.0/16", "-j", "ACCEPT", NULL);
        while(*allowed_ip != NULL){
              res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-d", *allowed_ip, "-j", "ACCEPT", NULL);
              allowed_ip++;
         }
         res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-o", "cc+", "-j", "DROP", NULL);
         res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-o", "ppp+", "-j", "DROP", NULL);
         }else{
         // volte-nsiot
        res |= execIptables(target, "-t", "mangle", "-F", LOCAL_MANGLE_POSTROUTING, NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-d", "10.0.0.0/8", "-j", "ACCEPT", NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-d", "172.16.0.0/12", "-j", "ACCEPT", NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-d", "192.168.0.0/16", "-j", "ACCEPT", NULL);
        while(*allowed_ip != NULL){
            res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-d", *allowed_ip, "-j", "ACCEPT", NULL);
              allowed_ip++;
         }
        res |= execIptables(V4V6, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-j", "DROP", NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string", "spirent",
              "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string", "slp.rs.de",
            "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string",
             "pub.3gppnetwork.org", "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(target, "-t", "mangle", "-A", LOCAL_MANGLE_POSTROUTING, "-o", "ppp+", "-j", "DROP", NULL);
        }
       openNsiotFlag = true;
        return res;
}

/*MTK: support nsiot*/
int FirewallController::clearNsiotFirewall(void) {
    int res = 0;
    IptablesTarget target = V4V6;

    if(openNsiotFlag)
    {
        res = execIptables(target, "-t", "mangle", "-F", LOCAL_MANGLE_POSTROUTING, NULL);
        openNsiotFlag = false;
    }else
       {
          ALOGE("clearNsiotFirewall invalid,openNsiotFlag = flase");
       }

    return res;
}

/*MTK: support nsiot*/
int FirewallController::setVolteNsiotFirewall(const char* iface){
    int res = 0;
    IptablesTarget target = V4;

     if(iface == NULL){
          ALOGE("setVolteNsiotFirewall: Error iface");
          return -1;
       }
     if(openNsiotVolteFlag){
         ALOGD("VolteNsiot already opened!");
           return 0;
       }
    res |= execIptables(V4V6, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string",
           "xcap", "--algo", "bm","-j", "ACCEPT",NULL);
    res |= execIptables(V4V6, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string", "--string",
           "bsf", "--algo", "bm","-j", "ACCEPT",NULL);
    res |= execIptables(target, "-t", "mangle", "-I", LOCAL_MANGLE_POSTROUTING, "-o", iface,"-j", "ACCEPT",NULL);
    openNsiotVolteFlag = true;
    return res;
}

/*MTK: support nsiot*/
int FirewallController::clearVolteNsiotFirewall(const char* iface){

    int res = 0;
    IptablesTarget target = V4;
    if(iface == NULL){
       ALOGE("clearVolteNsiotFirewall: Error iface");
       return -1;
     }
    if(openNsiotVolteFlag){
        res |= execIptables(target, "-t", "mangle", "-D", LOCAL_MANGLE_POSTROUTING, "-o", iface,"-j", "ACCEPT",NULL);
        res |= execIptables(V4V6, "-t", "mangle", "-D", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string",
                 "--string", "xcap", "--algo", "bm","-j", "ACCEPT",NULL);
        res |= execIptables(V4V6, "-t", "mangle", "-D", LOCAL_MANGLE_POSTROUTING, "-p", "udp", "--dport", "53", "-m", "string",
                 "--string", "bsf", "--algo", "bm","-j", "ACCEPT",NULL);
        openNsiotVolteFlag = false;
     }
      //skip fail
      return 0;
}

/*MTK: support nsiot*/
int FirewallController::getUsbClientIp(const char * iface){
        const char *file_path = "/proc/net/arp";
        char rawaddrstr[20];
        char tempaddrstr[20];
        char flag[8];
        char arp_line[128];
        char netdev[20];
        unsigned int line, found;
        FILE *f = fopen(file_path, "r");
        if (!f) {
           ALOGE("open file:%s failed\n", file_path);
          return -errno;
         }
         memset(rawaddrstr, 0, sizeof(rawaddrstr));
          // Format:
          // lease file: 1234567890 00:11:22:33:44:55 255.255.255.255 android-hostname *
          // arp: 255.255.255.255 0x1 0x2 00:11:22:33:44:55 * rndis0
         line = 0; found = 0;
         while(fgets(arp_line, sizeof(arp_line), f) != NULL){
             if(line == 0)
                  ALOGD("arp head: %s", arp_line);
                    else{
                       if(sscanf(arp_line, "%16s %*8s %8s %*18s %*16s %20s",
                                 rawaddrstr, flag, netdev) == 3){
                            ALOGD("IP addr = %s, flag=%s, dev=%s\n", rawaddrstr, flag, netdev);
                            if(0 == strncmp(netdev, "rndis", 5)){
                                       memset(tempaddrstr, 0, sizeof(tempaddrstr));
                                       strncpy(tempaddrstr, rawaddrstr, sizeof(tempaddrstr));
                                       found = 1;
                                       if(0 == strncmp(flag, "0x2", 3)){
                                        /*get the reachable client, break out now*/
                                         ALOGI("find rndis client: %s\n", tempaddrstr);
                                          break;
                                       }
                             }
                        }
                      memset(rawaddrstr, 0, sizeof(rawaddrstr));
                 }
                line++;
           }
          fclose(f);
        if(found == 1){
            property_set("net.rndis.client", tempaddrstr);
        } else {
            ALOGW("can't find %s client!", iface);
          }
         return 0;
}

/****************************************************************************
* MTK: support tencent firewall
* set FIREWALL_MOBILE and FIREWALL_WIFI rule to reject packets based on uid
*****************************************************************************/
int FirewallController::setUidFwRule(int uid, FirewallChinaRule chain, FirewallRule rule) {
    char uidStr[16];
    int res = 0;
    const char* op;
    const char* fwChain;

    sprintf(uidStr, "%d", uid);

    if (rule == ALLOW) {
        op = "-I";
    } else {
        op = "-D";
    }

    if(chain == MOBILE) {
        fwChain = "mobile";
    }else{
        fwChain = "wifi";
    }

    res |= execIptables(V4, op, fwChain, "-m", "owner", "--uid-owner", uidStr,
            "-j", "REJECT", "--reject-with", "icmp-net-prohibited", NULL);
    res |= execIptables(V6, op, fwChain, "-m", "owner", "--uid-owner", uidStr,
            "-j", "REJECT", "--reject-with", "icmp6-adm-prohibited", NULL);

    return res;
}

/************************************************************
* MTK: support tencent firewall
* flush FIREWALL_MOBILE and FIREWALL_WIFI
************************************************************/
int FirewallController::clearFwChain(const char* chain) {
    int res = 0;

    if(chain != NULL){
        if(strlen(chain) > 0){
            res |= execIptables(V4V6, "-F", chain, NULL);
        }else{
            ALOGD("Clear all chain");
            res |= execIptables(V4V6, "-F", NULL);
        }
    }else{
        ALOGE("Chain is NULL");
    }

    return res;
}

#define MAX_APP 12

/*MTK: support antutu blocking*/
int FirewallController::refreshPkgUidList(const char *file_path, int uid, bool add){
    unsigned int uids[MAX_APP];
    char iptables_cmd[128];
    int line, index, temp_uid;
    bool found;
    FILE *f = fopen(file_path, "a+");

    if (!f) {
        ALOGE("open file:%s failed for reading\n", file_path);
        return -errno;
    }
    rewind(f);
    //chmod(file_path, S_IRWXU|S_IRGRP|S_IXGRP|S_IROTH);

    memset(iptables_cmd, 0, sizeof(iptables_cmd));
    memset(uids, 0, sizeof(uids));
    // Format: iptables cmd: iptables -I oem_out -m owner --uid-owner 100xx -j DROP
    line = 0;
    index = 0;
    found = false;

    while(fgets(iptables_cmd, sizeof(iptables_cmd), f) != NULL){
            if(sscanf(iptables_cmd, "%*32s %*4s %*16s %*4s %*8s %*20s %05d %*4s %*8s",
                          &temp_uid) == 1){
            ALOGD("read file: line %d uid = %d\n", line, temp_uid);
            } else {
                   ALOGW("invalid format[%d]: %s\n", line, iptables_cmd);
                   continue;
            }
            if(add == true && temp_uid == uid){
                    ALOGD("add exist uid %d\n", uid);
                    fclose(f);
                    return 0;
            }
            if(add == false && temp_uid ==uid){
                ALOGD("find the uid %d to delete\n", uid);
                found = true;
                continue;
            }
            if(index < MAX_APP){
                    uids[index] = temp_uid;
                    index++;
            }else{
                //should not be here. but also do write for removing extra lines
                ALOGE("error: too many lines in script file\n");
                break;
            }
            line++;
            //for ip6tables line
            memset(iptables_cmd, 0, sizeof(iptables_cmd));
            if(fgets(iptables_cmd, sizeof(iptables_cmd), f) != NULL){
                if(strncmp(iptables_cmd, "ip6tables", 9) == 0){
                        line++;
                } else {
                        ALOGW("miss ip6tables\n");
                        break;
                }
            }else{
                  ALOGW("end of file: miss ip6tables\n");
                  break;
            }
       }

        if(found == false){
                if(add == true){
                       if(index < MAX_APP){
                                ALOGD("add new app uid %d\n", uid);
                                uids[index] = uid;
                                index++;
                        } else {
                                ALOGE("fail to add uid %d due to max app num\n", uid);
                                fclose(f);
return -1;
                        }
                }else{
                        ALOGW("uid %d is not found for deleting\n", uid);
                        fclose(f);
                        return 0;
                }
        }
        fclose(f);
        f = fopen(file_path, "w+");
    if (!f) {
        ALOGE("open file:%s failed for writing\n", file_path);
        return -errno;
    }
        {
                int i;
                char cmd_buff[128];
                for(i=0;i < index;i++){
                         snprintf(cmd_buff, sizeof(cmd_buff), "iptables -I oem_out -m owner --uid-owner %d -j DROP\n", uids[i]);
                         fputs(cmd_buff, f);
                         snprintf(cmd_buff, sizeof(cmd_buff), "ip6tables -I oem_out -m owner --uid-owner %d -j DROP\n", uids[i]);
                         fputs(cmd_buff, f);
            }
        }
        fclose(f);
        return 0;
}

/*MTK: support antutu blocking*/
int FirewallController::setPkgUidRule(int uid, FirewallRule rule) {
    char uidStr[16];
    int res = 0;
    sprintf(uidStr, "%d", uid);

    if (rule == ALLOW) {
        execIptables(V4V6, "-D", "oem_out", "-m", "owner", "--uid-owner", uidStr,
                        "-j", "DROP", NULL);
        refreshPkgUidList(IPT_PATH, uid, false);
    } else {
            //ignore error result of deleting rules
            execIptables(V4V6, "-D", "oem_out", "-m", "owner", "--uid-owner", uidStr,
                    "-j", "DROP", NULL);
            execIptables(V4V6, "-I", "oem_out", "-m", "owner", "--uid-owner", uidStr,
                    "-j", "DROP", NULL);
            refreshPkgUidList(IPT_PATH, uid, true);
    }

    return res;
}

/*MTK: support pptp vpn*/
int FirewallController::setEgressProtoRule(const char* proto, FirewallRule rule) {
    int protocol = 0;
    IptablesTarget target = V4;

    ALOGI("setEgressProtoRule:%s", proto);

    if(!strcmp(proto, "tcp")){
        protocol = PROTOCOL_TCP;
    }else if(!strcmp(proto, "udp")){
        protocol = PROTOCOL_UDP;
    }else if(!strcmp(proto, "gre")){
        protocol = PROTOCOL_GRE;
    }else if(!strcmp(proto, "icmp")){
        protocol = PROTOCOL_ICMP;
    }

    if(protocol == 0){
        return 0;
    }

    char protocolStr[16];
    sprintf(protocolStr, "%d", protocol);

    ALOGI("setEgressProtoRule:%s:%d", proto, protocol);

    const char* op;
    if (rule == ALLOW) {
        op = "-I";
    } else {
        op = "-D";
    }

    int res = 0;
    res |= execIptables(target, op, LOCAL_INPUT, "-p", protocolStr,
            "-j", "RETURN", NULL);
    res |= execIptables(target, op, LOCAL_OUTPUT, "-p", protocolStr,
            "-j", "RETURN", NULL);
    res |= execIptables(target, "-t", "mangle", op, LOCAL_MANGLE_POSTROUTING, "-p", protocolStr,
            "-j", "RETURN", NULL);

    return res;
}
