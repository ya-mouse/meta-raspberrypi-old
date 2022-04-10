SUMMARY = "PowerTune is a Modern Gauge Display"
SECTION = "libs"
HOMEPAGE = "https://github.com/BastianGschrey/PowerTune"

# GPLv3
LICENSE = "GPLv3"
LIC_FILES_CHKSUM = "file://LICENSE;md5=84dcc94da3adb52b53ae4fa38fe49e5d"

DEPENDS = "qtbase qttools"
DEPENDS += " qtserialbus qtcharts qtlocation qtsensors qtmultimedia qtquickcontrols2 qtdeclarative qtgraphicaleffects qtsvg"
RDEPENDS_${PN} += " sudo qtvirtualkeyboard qtsvg-plugins qtxmlpatterns qtdeclarative-qmlplugins qtgraphicaleffects-qmlplugins qtquickcontrols-qmlplugins qtlocation-qmlplugins qtsensors-qmlplugins qtbase-qmlplugins qtbase-plugins libsocketcan"

inherit qmake5
inherit useradd

SRCREV = "${AUTOREV}"
SRC_URI = "git://github.com/BastianGschrey/PowerTune;protocol=https"

S = "${WORKDIR}/git"

USERADD_PACKAGES = "${PN}"

USERADD_PARAM_${PN} = "-d /home/pi -s /bin/bash -P 'powertune' pi"

do_install_append() {

    install -m 0755 -pD ${S}/daemons/EMUCANd ${D}/home/pi/daemons/EMUCANd

    # Add sudoers config
    install -dm 0750 ${D}${sysconfdir}/sudoers.d
    cat<<EOF>${D}${sysconfdir}/sudoers.d/powertune
pi ALL=(ALL) ALL
EOF


    # Install InitV scripts
    for d in init.d rc3.d rc5.d; do \
        install -dm 0755 ${D}${sysconfdir}/${d}; \
    done
    cat <<EOF>${D}${sysconfdir}/init.d/powertune
export LC_ALL=en_US.utf8
export QT_QPA_EGLFS_PHYSICAL_WIDTH=155
export QT_QPA_EGLFS_PHYSICAL_HEIGHT=86
export QT_QPA_EGLFS_HIDECURSOR=1
export QT_QPA_EGLFS_ALWAYS_SET_MODE=1
export QT_QPA_EGLFS_KMS_ATOMIC=1
export QT_QPA_PLATFORM=eglfs

#Check if there are compiled binaries available
if [ -f /home/pi/build/PowertuneQMLGui ];then
#copy binaries and delete build folder
cp -R /home/pi/build/PowertuneQMLGui /opt/PowerTune
rm -r /home/pi/build
fi
#Start PowerTune
(cd /opt/PowerTune; ./PowertuneQMLGui) &

# Allow QT5 have more IOPS to load all lib/plugins while starting in background
#sleep 1.5
#start the communication daemon
/home/pi/startdaemon.sh &

# Wait a bit before processing next init script
#sleep 1


EOF
    chmod 0755 ${D}${sysconfdir}/init.d/powertune
    ln -s ../init.d/powertune ${D}${sysconfdir}/rc3.d/S010powertune
    ln -s ../init.d/powertune ${D}${sysconfdir}/rc5.d/S010powertune
}

FILES_${PN} += "/opt/PowertuneQMLGui /opt/PowerTune /home/pi/daemons"
