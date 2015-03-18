/**
 * Copyright (c) 2015 Source Auditor Inc.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
*/
package org.spdx.spdxeclipse.project;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.spdx.rdfparser.DOAPProject;
import org.spdx.rdfparser.SPDXFile;
import org.spdx.rdfparser.SPDXLicenseInfo;
import org.spdx.rdfparser.SPDXLicenseInfoFactory;
import org.spdx.rdfparser.SpdxRdfConstants;
import org.spdx.spdxeclipse.properties.SpdxFileProperties;
import org.spdx.spdxeclipse.properties.SpdxProjectProperties;
import org.spdx.spdxspreadsheet.InvalidLicenseStringException;

/**
 * Static helper methods for SPDX Eclipse
 * @author Gary O'Neall
 *
 */
public class SpdxHelper {
	
	static HashSet<String> SOURCE_EXTENSION = new HashSet<String>();
	
	static {
		SOURCE_EXTENSION.add("C"); SOURCE_EXTENSION.add("H");
		SOURCE_EXTENSION.add("JAVA"); SOURCE_EXTENSION.add("CS");
		SOURCE_EXTENSION.add("JS"); SOURCE_EXTENSION.add("HH");
		SOURCE_EXTENSION.add("CC"); SOURCE_EXTENSION.add("CPP");
		SOURCE_EXTENSION.add("CXX"); SOURCE_EXTENSION.add("HPP");
		SOURCE_EXTENSION.add("ASP"); SOURCE_EXTENSION.add("BAS");
		SOURCE_EXTENSION.add("BAT"); SOURCE_EXTENSION.add("HTM");
		SOURCE_EXTENSION.add("HTML"); SOURCE_EXTENSION.add("LSP");
		SOURCE_EXTENSION.add("PAS"); SOURCE_EXTENSION.add("XML");
		SOURCE_EXTENSION.add("PAS"); SOURCE_EXTENSION.add("ADA");
		SOURCE_EXTENSION.add("VB"); SOURCE_EXTENSION.add("ASM");
		SOURCE_EXTENSION.add("CBL"); SOURCE_EXTENSION.add("COB");
		SOURCE_EXTENSION.add("F77"); SOURCE_EXTENSION.add("M3");
		SOURCE_EXTENSION.add("MK"); SOURCE_EXTENSION.add("MKE");
		SOURCE_EXTENSION.add("RMK"); SOURCE_EXTENSION.add("MOD");
		SOURCE_EXTENSION.add("PL"); SOURCE_EXTENSION.add("PM");
		SOURCE_EXTENSION.add("PRO"); SOURCE_EXTENSION.add("REX");
		SOURCE_EXTENSION.add("SM"); SOURCE_EXTENSION.add("ST");
		SOURCE_EXTENSION.add("SNO"); SOURCE_EXTENSION.add("PY");
		SOURCE_EXTENSION.add("PHP"); SOURCE_EXTENSION.add("CSS");
		SOURCE_EXTENSION.add("XSL"); SOURCE_EXTENSION.add("XSLT");
		SOURCE_EXTENSION.add("SH"); SOURCE_EXTENSION.add("XSD");
		SOURCE_EXTENSION.add("RB"); SOURCE_EXTENSION.add("RBX");		
		SOURCE_EXTENSION.add("RHTML"); SOURCE_EXTENSION.add("RUBY");
	}
	
	static HashSet<String> BINARY_EXTENSIONS = new HashSet<String>();
	static {
		BINARY_EXTENSIONS.add("EXE");	BINARY_EXTENSIONS.add("DLL");
		BINARY_EXTENSIONS.add("JAR");	BINARY_EXTENSIONS.add("CLASS");
		BINARY_EXTENSIONS.add("SO");	BINARY_EXTENSIONS.add("A");
	}
	
	static HashSet<String> ARCHIVE_EXTENSIONS = new HashSet<String>();
	static {
		ARCHIVE_EXTENSIONS.add("ZIP"); ARCHIVE_EXTENSIONS.add("EAR");
		ARCHIVE_EXTENSIONS.add("TAR"); ARCHIVE_EXTENSIONS.add("GZ");
		ARCHIVE_EXTENSIONS.add("TGZ"); ARCHIVE_EXTENSIONS.add("BZ2");
		ARCHIVE_EXTENSIONS.add("RPM"); 
	}
	
	static final String SHA1_ALGORITHM = "SHA-1";
	static final String PACKAGE_VERIFICATION_CHARSET = "UTF-8";
	private static MessageDigest digest;
	static {
		try {
			digest = MessageDigest.getInstance(SHA1_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			digest = null;
		};
	}
	
	/**
	 * Convert a File resource (implements IFile interface) to an SPDX File
	 * @param file
	 * @return
	 * @throws SpdxProjectException 
	 */
	public static SPDXFile convertFile(IFile file) throws SpdxProjectException {
		String relativePath = file.getProjectRelativePath().toString();
		String fileType = extensionToFileType(file.getFileExtension());
		String sha1 = generateSha1(file);
		SPDXLicenseInfo license;
		try {
			String licenseName = SpdxFileProperties.getConcludedLicense(file);
			if (licenseName == null || licenseName.trim().isEmpty()) {
				licenseName = SpdxProjectProperties.getDefaultFileLicense(file.getProject());
			}
			license = SPDXLicenseInfoFactory.parseSPDXLicenseString(licenseName);
		} catch (InvalidLicenseStringException e) {
			throw new SpdxProjectException("Invalid license for file - update file properties to a correct license: "+e.getMessage());
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the concluded license: "+e.getMessage());
		}
		String copyright;
		try {
			copyright = SpdxFileProperties.getCopyright(file);
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the file copyright: "+e.getMessage());
		}
		String notice;
		try {
			notice = SpdxFileProperties.getNotice(file);
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the file notice: "+e.getMessage());
		}
		String comment;
		try {
			comment = SpdxFileProperties.getComment(file);
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the file comment: "+e.getMessage());
		}
		String[] contributors;
		try {
			contributors = SpdxFileProperties.getContributors(file);
			if (contributors == null) {
				contributors = new String[0];
			}
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the file contributors: "+e.getMessage());
		}
		DOAPProject[] artifactOf;
		String osProject;
		try {
			osProject = SpdxFileProperties.getProjectName(file);
		} catch (CoreException e) {
			throw new SpdxProjectException("Error getting file property for the related project name: "+e.getMessage());
		}
		if (osProject != null && !osProject.isEmpty()) {
			String osProjectUrl;
			try {
				osProjectUrl = SpdxFileProperties.getProjectUrl(file);
			} catch (CoreException e) {
				throw new SpdxProjectException("Error getting file property for the related project URL: "+e.getMessage());
			}
			artifactOf = new DOAPProject[] {new DOAPProject(osProject, osProjectUrl)};
		} else {
			artifactOf = new DOAPProject[0];
		}
		// Setting the concluded license to NOASSERTION since it has not been 
		// formally reviewed
		SPDXLicenseInfo concludedLicense = license;
		
		return new SPDXFile(relativePath, fileType, 
				sha1, concludedLicense, new SPDXLicenseInfo[] {license}, "", copyright,
				artifactOf, comment, null, contributors, notice);
	}

	public static String generateSha1(IFile file) throws SpdxProjectException {
		if (digest == null) {
			try {
				digest = MessageDigest.getInstance(SHA1_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				throw(new SpdxProjectException("Unable to create the message digest for generating the File SHA1"));
			}
		}
		digest.reset();
		InputStream in;
		try {
			in = file.getContents();
		} catch (CoreException e1) {
			throw(new SpdxProjectException("IO getting file content while calculating the SHA1"));
		}
		try {
			byte[] buffer = new byte[2048];
			int numBytes = in.read(buffer);
			while (numBytes >= 0) {
				digest.update(buffer, 0, numBytes);
				numBytes = in.read(buffer);
			}
			return convertChecksumToString(digest.digest());
		} catch (IOException e) {
			throw(new SpdxProjectException("IO error reading file input stream while calculating the SHA1"));
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				throw(new SpdxProjectException("IO error closing file input stream while calculating the SHA1"));
			}
		}
	}

	public static String convertChecksumToString(byte[] digestBytes) {
		StringBuilder sb = new StringBuilder();   
		for (int i = 0; i < digestBytes.length; i++) {
			String hex = Integer.toHexString(0xff & digestBytes[i]);
			if (hex.length() < 2) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	private static String extensionToFileType(String fileExtension) {
		if (fileExtension == null) {
			return SpdxRdfConstants.FILE_TYPE_OTHER;
		}
		String upperExtension = fileExtension.toUpperCase();
		if (SOURCE_EXTENSION.contains(upperExtension)) {
			return SpdxRdfConstants.FILE_TYPE_SOURCE;
		} else if (BINARY_EXTENSIONS.contains(upperExtension)) {
			return SpdxRdfConstants.FILE_TYPE_BINARY;
		} else if (ARCHIVE_EXTENSIONS.contains(upperExtension)) {
			return SpdxRdfConstants.FILE_TYPE_ARCHIVE;
		} else {
			return SpdxRdfConstants.FILE_TYPE_OTHER;
		}
	}

}
