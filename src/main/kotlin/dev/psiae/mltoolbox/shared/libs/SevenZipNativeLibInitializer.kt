package dev.psiae.mltoolbox.shared.libs

import dev.psiae.mltoolbox.shared.java.jFile
import dev.psiae.mltoolbox.shared.utils.unsupportedOperationError
import net.sf.sevenzipjbinding.SevenZip
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException
import java.io.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


// https://github.com/borisbrodski/sevenzipjbinding/issues/31
// https://github.com/borisbrodski/sevenzipjbinding/pull/34
// https://github.com/NationalSecurityAgency/ghidra/blob/aac2cf7e9bd9b97a8a74234e7ffd52a0f0bada5f/Ghidra/Features/FileFormats/src/main/java/ghidra/file/formats/sevenzip/SevenZipCustomInitializer.java
object SevenZipNativeLibInitializer {

    const val SYSTEM_PROPERTY_TMP = "java.io.tmpdir"
    const val PROPERTY_SEVENZIPJBINDING_LIB_NAME = "lib.%s.name"
    const val PROPERTY_SEVENZIPJBINDING_LIB_HASH = "lib.%s.hash"
    const val SEVENZIPJBINDING_LIB_PROPERTIES_FILENAME = "sevenzipjbinding-lib.properties"
    const val PROPERTY_BUILD_REF = "build.ref"

    @Throws(SevenZipNativeInitializationException::class)
    fun init() {
        if (SevenZip.isInitializedSuccessfully())
            return
        try {
            checkRuntimePlatformIsSupported()
            val platform = SevenZip.getPlatformBestMatch()
            val properties = loadProperties(platform)
            // TODO: make constants
            val tmpDir = jFile("mltoolboxapp\\nativelibs\\sevenzip\\$platform")
            tmpDir.mkdirs()
            val tmpDirFile = createOrVerifyTmpDir(tmpDir)
            val sevenZipJBindingTmpDir = getOrCreateSevenZipJBindingTmpDir(tmpDirFile, properties)
            val nativeLibraries = ArrayList<File>(5)
            run {
                var i = 1
                while (true) {
                    val propertyName = String.format(PROPERTY_SEVENZIPJBINDING_LIB_NAME, i)
                    val propertyHash = String.format(PROPERTY_SEVENZIPJBINDING_LIB_HASH, i)
                    val libName: String? = properties.getProperty(propertyName)
                    val libHash: String? = properties.getProperty(propertyHash)
                    if (libName == null) {
                        if (nativeLibraries.isEmpty()) {
                            throw SevenZipNativeInitializationException(
                                "property file '$SEVENZIPJBINDING_LIB_PROPERTIES_FILENAME' from 'sevenzipjbinding-<Platform>.jar missing property '$propertyName'"
                            )
                        } else {
                            break
                        }
                    }
                    if (libHash == null) {
                        throw SevenZipNativeInitializationException(
                            "property file '$SEVENZIPJBINDING_LIB_PROPERTIES_FILENAME' from ' from 'sevenzipjbinding-<Platform>.jar' missing property '$propertyHash' containing the hash for the library '$libName'"
                        )
                    }
                    val libTmpFile: File = File("$sevenZipJBindingTmpDir\\$libName")
                    if (!libTmpFile.exists() || !hashMatched(libTmpFile, libHash)) {
                        val libInputStream =
                            SevenZip::class.java.getResourceAsStream("/$platform/$libName")
                                ?: throw SevenZipNativeInitializationException(
                                    ("error loading native library '" + libName
                                            + "' from a jar-file 'sevenzipjbinding-<Platform>.jar'.")
                                )
                        copyLibraryToFS(libTmpFile, libInputStream)
                    }
                    nativeLibraries.add(libTmpFile)
                    i++
                }
            }
            loadNativeLibraries(nativeLibraries)
            SevenZip.initLoadedLibraries()
        } catch (e: Exception) {
            // should we also catch RuntimeException ?
            if (e is IOException || e is UnsupportedOperationException || e is SevenZipNativeInitializationException || e is RuntimeException) {
                throw SevenZipNativeInitializationException("error initializing SevenZip native library", e)
            }
        }
    }

    // we only support AMD64 (64-bit) Windows
    private fun checkRuntimePlatformIsSupported() {
        val osName = System.getProperty("os.name")
        val arch = System.getProperty("os.arch")

        fun unsupportedOSErr() {
            unsupportedOperationError("Unsupported OS=$osName arch=$arch")
        }
        when {
            osName.startsWith("Windows") -> {
                if (arch.equals("amd64", true))
                    return
                unsupportedOSErr()
            }
            else -> unsupportedOSErr()
        }
    }

    @Throws(SevenZipNativeInitializationException::class, IOException::class)
    private fun loadProperties(platform: String): Properties {
        // prop file contains lib.##.name and lib.##.hash values
        val propFilename = "/$platform/sevenzipjbinding-lib.properties"
        SevenZip::class.java.getResourceAsStream(propFilename).use { propFileStream ->
            if (propFileStream == null) {
                throw IOException("Error loading property file stream $propFilename")
            }
            val properties = Properties()
            properties.load(propFileStream)
            return properties
        }
    }

    @Throws(IOException::class)
    private fun getNativeLibraryInfo(properties: Properties): Map<String, String> {
        // LinkedHashMap to preserve order
        val libraryInfo = LinkedHashMap<String, String>()
        var libNum = 1
        var libName: String
        while ((properties.getProperty(String.format("lib.%d.name", libNum)).also { libName = it }) != null) {
            val libHash = properties.getProperty(String.format("lib.%d.hash", libNum))
                ?: throw IOException(
                    "Missing library hash value in property file for library lib." + libNum +
                            ".name=" + libName
                )
            libraryInfo[libName] = libHash
            libNum++
        }
        if (libraryInfo.isEmpty()) {
            throw IOException("Missing library hash values in property file")
        }
        return libraryInfo
    }

    @Throws(SevenZipNativeInitializationException::class)
    private fun loadNativeLibraries(libFiles: List<jFile>) {
        // Load native libraries in reverse order (per the logic in upstream's initialization code)
        for (i in libFiles.indices.reversed()) {
            val libFile: jFile = libFiles[i]
            try {
                System.load(libFile.absolutePath)
            } catch (t: Throwable) {
                throw SevenZipNativeInitializationException(
                    "Error loading native library: $libFile", t
                )
            }
        }
    }

    @Throws(SevenZipNativeInitializationException::class)
    private fun hashMatched(libTmpFile: File, libHash: String): Boolean {
        val digest: MessageDigest
        try {
            digest = MessageDigest.getInstance("SHA1")
        } catch (e: NoSuchAlgorithmException) {
            throw SevenZipNativeInitializationException(
                "Error initializing SHA1 algorithm"
            )
        }

        val fileInputStream: FileInputStream?
        try {
            fileInputStream = FileInputStream(libTmpFile)
        } catch (e: IOException) {
            throw SevenZipNativeInitializationException(
                "Error opening library file from the temp directory for reading: '${libTmpFile.absolutePath}'",
                e
            )
        }

        var ok = false
        try {
            val buffer = ByteArray(128 * 1024)
            while (true) {
                var length: Int
                try {
                    length = fileInputStream.read(buffer)
                } catch (e: IOException) {
                    throw SevenZipNativeInitializationException(
                        "Error reading from library file opened from the temp directory: '${libTmpFile.absolutePath}'",
                        e
                    )
                }
                if (length <= 0) {
                    break
                }
                digest.update(buffer, 0, length)
            }

            val result =
                byteArrayToHex(digest.digest()) == libHash.trim { it <= ' ' }.lowercase(Locale.getDefault())

            ok = true
            return result
        } finally {
            try {
                fileInputStream.close()
            } catch (e: IOException) {
                if (ok) {
                    throw SevenZipNativeInitializationException(
                        "Error closing library file from the temp directory (opened for reading): '${libTmpFile.absolutePath}'",
                        e
                    )
                }
            }
        }
    }

    private fun byteArrayToHex(byteArray: ByteArray): String {
        return StringBuilder()
            .apply {
                for (i in byteArray.indices) {
                   append(String.format("%1$02x", 0xFF and byteArray[i].toInt()))
                }
            }
            .toString()
    }

    @Throws(SevenZipNativeInitializationException::class)
    private fun createOrVerifyTmpDir(tmpDirectory: File?): File {
        val tmpDirFile: File
        if (tmpDirectory != null) {
            tmpDirFile = tmpDirectory
        } else {
            val systemPropertyTmp = System.getProperty(SYSTEM_PROPERTY_TMP)
                ?: throw SevenZipNativeInitializationException(
                    "can't determinte tmp directory. Use may use -D" + SYSTEM_PROPERTY_TMP
                            + "=<path to tmp dir> parameter for jvm to fix this."
                )
            tmpDirFile = File(systemPropertyTmp)
        }

        if (!tmpDirFile.exists() || !tmpDirFile.isDirectory) {
            throw SevenZipNativeInitializationException(
                "invalid tmp directory '$tmpDirectory'"
            )
        }

        if (!tmpDirFile.canWrite()) {
            throw SevenZipNativeInitializationException(
                "can't create files in '" + tmpDirFile.absolutePath + "'"
            )
        }
        return tmpDirFile
    }

    @Throws(SevenZipNativeInitializationException::class)
    private fun getOrCreateSevenZipJBindingTmpDir(tmpDirFile: File, properties: Properties): File {
        val buildRef = getOrGenerateBuildRef(properties)
        val tmpSubdirFile = File(tmpDirFile.absolutePath + File.separator + "SevenZipJBinding-" + buildRef)
        if (!tmpSubdirFile.exists()) {
            if (!tmpSubdirFile.mkdir()) {
                throw SevenZipNativeInitializationException("Directory '" + tmpDirFile.absolutePath + "' couldn't be created")
            }
        }
        return tmpSubdirFile
    }

    private fun getOrGenerateBuildRef(properties: Properties): String {
        var buildRef = properties.getProperty(PROPERTY_BUILD_REF)
        if (buildRef == null) {
            buildRef = Random().nextInt(10000000).toString()
        }
        return buildRef
    }

    private fun copyLibraryToFS(toLibTmpFile: File, fromLibInputStream: InputStream) {
        var libTmpOutputStream: FileOutputStream? = null
        try {
            libTmpOutputStream = FileOutputStream(toLibTmpFile)
            val buffer = ByteArray(65536)
            while (true) {
                val read = fromLibInputStream.read(buffer)
                if (read > 0) {
                    libTmpOutputStream.write(buffer, 0, read)
                } else {
                    break
                }
            }
        } catch (e: Exception) {
            throw RuntimeException(
                ("Error initializing SevenZipJBinding native library: "
                        + "can't copy native library out of a resource file to the temporary location: '"
                        + toLibTmpFile.absolutePath + "'"), e
            )
        } finally {
            try {
                fromLibInputStream.close()
            } catch (e: IOException) {
                // Ignore errors here
            }
            try {
                libTmpOutputStream?.close()
            } catch (e: IOException) {
                // Ignore errors here
            }
        }
    }
}