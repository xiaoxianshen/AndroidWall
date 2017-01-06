#!/system/bin/sh
IPTABLES=iptables
BUSYBOX=busybox
GREP=grep
ECHO=echo
# Try to find busybox
if /data/user/0/com.googlecode.droidwall/app_bin/busybox_g1 --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/data/user/0/com.googlecode.droidwall/app_bin/busybox_g1
	GREP="$BUSYBOX grep"
	ECHO="$BUSYBOX echo"
elif busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=busybox
elif /system/xbin/busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/system/xbin/busybox
elif /system/bin/busybox --help >/dev/null 2>/dev/null ; then
	BUSYBOX=/system/bin/busybox
fi
# Try to find grep
if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then
	if $ECHO 1 | $BUSYBOX grep -q 1 >/dev/null 2>/dev/null ; then
		GREP="$BUSYBOX grep"
	fi
	# Grep is absolutely required
	if ! $ECHO 1 | $GREP -q 1 >/dev/null 2>/dev/null ; then
		$ECHO The grep command is required. DroidWall will not work.
		exit 1
	fi
fi
# Try to find iptables
# Added if iptables binary already in system then use it, if not use implemented one
if ! command -v iptables &> /dev/null; then
if /data/user/0/com.googlecode.droidwall/app_bin/iptables_armv5 --version >/dev/null 2>/dev/null ; then
	IPTABLES=/data/user/0/com.googlecode.droidwall/app_bin/iptables_armv5
fi
fi
$IPTABLES --version || exit 1
# Create the droidwall chains if necessary
$IPTABLES -L droidwall >/dev/null 2>/dev/null || $IPTABLES --new droidwall || exit 2
$IPTABLES -L droidwall-3g >/dev/null 2>/dev/null || $IPTABLES --new droidwall-3g || exit 3
$IPTABLES -L droidwall-wifi >/dev/null 2>/dev/null || $IPTABLES --new droidwall-wifi || exit 4
$IPTABLES -L droidwall-reject >/dev/null 2>/dev/null || $IPTABLES --new droidwall-reject || exit 5
# Add droidwall chain to OUTPUT chain if necessary
$IPTABLES -L OUTPUT | $GREP -q droidwall || $IPTABLES -A OUTPUT -j droidwall || exit 6
# Flush existing rules
$IPTABLES -F droidwall || exit 7
$IPTABLES -F droidwall-3g || exit 8
$IPTABLES -F droidwall-wifi || exit 9
$IPTABLES -F droidwall-reject || exit 10
# Create the reject rule (log disabled)
$IPTABLES -A droidwall-reject -j REJECT || exit 11
# Main rules (per interface)
$IPTABLES -A droidwall -o rmnet+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o pdp+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o ppp+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o uwbr+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o wimax+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o vsnet+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o ccmni+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o usb+ -j droidwall-3g || exit
$IPTABLES -A droidwall -o tiwlan+ -j droidwall-wifi || exit
$IPTABLES -A droidwall -o wlan+ -j droidwall-wifi || exit
$IPTABLES -A droidwall -o eth+ -j droidwall-wifi || exit
$IPTABLES -A droidwall -o ra+ -j droidwall-wifi || exit
# Filtering rules
$IPTABLES -A droidwall-3g -m owner --uid-owner 10048 -j droidwall-reject || exit
$IPTABLES -A droidwall-wifi -m owner --uid-owner 10048 -j droidwall-reject || exit
exit
