package org.allcolor.yahp.converter;

public final class CSystem {
	private static final String OS = CSystem.getOS();

	private static final String arch = CSystem.getArchitecture();

	private static final String name = CSystem.getName();

	public static String getOS() {
		if (CSystem.OS != null) {
			return CSystem.OS;
		}
		String OS = java.lang.System.getProperty("os.name", "unknown")
				.toLowerCase();
		if (OS.indexOf("linux") != -1) {
			OS = "linux";
		} else if ((OS.indexOf("windows") != -1) && (OS.indexOf("ce") != -1)) {
			OS = "windowsce";
		} else if (OS.indexOf("windows") != -1) {
			OS = "windows";
		} else if ((OS.indexOf("solaris") != -1) || (OS.indexOf("sunos") != -1)) {
			OS = "solaris";
		} else if (((OS.indexOf("mac") != -1) && (OS.indexOf("os") != -1) && (OS
				.indexOf("x") != -1))
				|| (OS.indexOf("rhapsody") != -1)
				|| (OS.indexOf("darwin") != -1)) {
			OS = "macosx";
		} else if (OS.indexOf("mac") != -1) {
			OS = "macos";
		} else if ((OS.indexOf("free") != -1) && (OS.indexOf("bsd") != -1)) {
			OS = "freebsd";
		} else if ((OS.indexOf("net") != -1) && (OS.indexOf("bsd") != -1)) {
			OS = "netbsd";
		} else if ((OS.indexOf("open") != -1) && (OS.indexOf("bsd") != -1)) {
			OS = "openbsd";
		} else if (OS.indexOf("aix") != -1) {
			OS = "aix";
		} else if (OS.indexOf("digital unix") != -1) {
			OS = "digitalunix";
		} else if ((OS.indexOf("hp") != -1) && (OS.indexOf("ux") != -1)) {
			OS = "hpux";
		} else if (OS.indexOf("irix") != -1) {
			OS = "irix";
		} else if (OS.indexOf("os/2") != -1) {
			OS = "os_2";
		} else if ((OS.indexOf("open") != -1) && (OS.indexOf("vms") != -1)) {
			OS = "openvms";
		} else if (OS.indexOf("os/390") != -1) {
			OS = "os_390";
		} else {
			OS = OS.replace('/', '_').replace(' ', '_').replace('-', '_');
		}
		return OS;
	}

	public static String getArchitecture() {
		if (CSystem.arch != null) {
			return CSystem.arch;
		}
		String arch = java.lang.System.getProperty("os.arch", "unknown")
				.toLowerCase();
		if (((arch.indexOf("ia") != -1) && (arch.indexOf("64") != -1))
				|| (arch.indexOf("ia64") != -1)) {
			arch = "ia_64";
		} else if ((arch.indexOf("amd64") != -1)
				|| ((arch.indexOf("86") != -1) && (arch.indexOf("64") != -1))) {
			arch = "x86_64";
		} else if ((arch.indexOf("86") != -1) || (arch.indexOf("ia32") != -1)
				|| ((arch.indexOf("ia") != -1) && (arch.indexOf("32") != -1))) {
			arch = "x86";
		} else if ((arch.indexOf("ppc") != -1) || (arch.indexOf("power") != -1)) {
			arch = "ppc";
		} else if (arch.indexOf("sparc") != -1) {
			arch = "sparc";
		} else if (arch.indexOf("arm") != -1) {
			arch = "arm";
		} else if (arch.indexOf("alpha") != -1) {
			arch = "alpha";
		} else if ((arch.indexOf("pa") != -1) && (arch.indexOf("risc") != -1)
				&& (arch.indexOf("2") != -1)) {
			arch = "parisc2";
		} else if ((arch.indexOf("pa") != -1) && (arch.indexOf("risc") != -1)) {
			arch = "parisc";
		} else if (arch.indexOf("mips") != -1) {
			arch = "mips";
		} else if (arch.indexOf("02.10.00") != -1) {
			arch = "os_390";
		} else {
			arch = arch.replace('/', '_').replace(' ', '_').replace('-', '_');
		}
		return arch;
	}

	public static String getName() {
		if (CSystem.name != null) {
			return CSystem.name;
		}
		return CSystem.getOS() + "-" + CSystem.getArchitecture();
	}
}
