package korlibs.korge.lipsync

/*
import korlibs.audio.format.*
import korlibs.audio.format.mp3.*
import korlibs.audio.sound.*
import korlibs.korge.resources.*
import korlibs.io.dynamic.*
import korlibs.io.file.*
import korlibs.io.file.std.*
import korlibs.io.serialization.json.*
import korlibs.io.util.*
import korlibs.crypto.*
import korlibs.crypto.encoding.*
import korlibs.encoding.unhex
import korlibs.memory.Platform
import korlibs.platform.Platform
import java.io.*
import java.net.*

open class LipsyncResourceProcessor : ResourceProcessor("voice.wav", "voice.mp3", "voice.ogg") {
	companion object : LipsyncResourceProcessor() {
		private val nativeAudioFormats = AudioFormats().register(
            WAV, FastMP3Decoder,
			//ServiceLoader.load(korlibs.audio.format.AudioFormat::class.java).toList()
		)
	}

	override val version: Int = 0
	override val outputExtension: String = "lipsync"

	override suspend fun processInternal(inputFile: VfsFile, outputFile: VfsFile) {
		inputFile.copyTo(outputFile.parent[inputFile.baseName])
		outputFile.writeString(processAudioData(inputFile.readAudioData(nativeAudioFormats)).toLipString())
	}

	data class Config(val url: URL, val folder: String, val sha1: ByteArray, val exe: String)

	val config by lazy {
        val base = Config(
            URL("https://github.com/korlibs/korge-tools/releases/download/rhubarb-lip-sync-1.13/Rhubarb-Lip-Sync-1.13.0-all.zip"),
            "Rhubarb-lip-sync-1.13.0-all",
            "6A1850D829D361E36C09D7561C817C47C2D16FD0".unhex,
            "rhubarb"
        )
		when {
			Platform.isMac -> base.copy(exe = "rhubarb-mac")
			Platform.isLinux -> base.copy(exe = "rhubarb-linux")
			Platform.isWindows -> base.copy(exe = "rhubarb-win.exe")
			else -> error("Operating system '${Platform.rawOsName}', '${Platform.rawPlatformName}' not supported")
		}
	}


	data class Tool(val rhubarb: VfsFile)

	private val toolCache = AsyncOnce<Tool>()

	suspend fun getRhubarbTool(): Tool = toolCache {
		val toolsRoot = BINARY_ROOT
		val rootOutputFolder = toolsRoot
		val outputFolder = toolsRoot[config.folder]
		val localZipFile = toolsRoot[config.url.basename]

		if (!outputFolder.exists()) {
			if (!localZipFile.exists()) {
				println("Downloading ${config.url} ...")
				localZipFile.writeBytes(config.url.openStream().use {
					val data = it.readBytes()
					val expectedSha1 = config.sha1.hex
					val actualSha1 = data.sha1().hex
					if (expectedSha1 != actualSha1) {
						error("Downloaded file ${config.url} sha1 $actualSha1 doesn't match the expected sha1 $expectedSha1")
					}
					data
				})
			}


			//val mem = MemoryVfs()
			println("Extracting $localZipFile ...")
			val zip = localZipFile.openAsZip()
			//localZipFile.openAsZip().copyToTree(rootOutputFolder)
			zip.copyToRecursively(rootOutputFolder)

			println("Done")

			//val executableFile = File(rootOutputFolder[config.exe].absolutePath)

			//println("Making executable $executableFile ...")
			//executableFile.setExecutable(true, false)
		}



		//val zip = LocalVfs("c:/temp/rhubarb-lip-sync-1.4.2-win32.zip").openAsZip()
		//println(zip["rhubarb-lip-sync-1.4.2-osx"].exists())
		//println(zip.list().toList())
		//zip.copyToTree(KorgeBuildTools.BINARY_ROOT())

		return@toolCache Tool(outputFolder[config.exe])
	}

	fun VfsFile.toJvmFile() = File(this.absolutePath)

	suspend fun processWav(wavFile: VfsFile): String {
		val rhubarb = getRhubarbTool().rhubarb

		rhubarb.toJvmFile().setExecutable(true, false)
		val result = rhubarb.parent.execToString(rhubarb.absolutePath, wavFile.absolutePath)
		return result
	}

	suspend fun processAudioData(data: AudioData): RhubarbFile {
		val rhubarb = getRhubarbTool().rhubarb
		val tempFile = tempVfs["rhubarb-file.wav"]
		try {
			tempFile.write(data.toWav())
			rhubarb.toJvmFile().setExecutable(true, false)
			val result = tempVfs.execToString(listOf(rhubarb.absolutePath, "-f", "json", tempFile.absolutePath))
            val info = Json.parse(result).dyn
            return RhubarbFile(
                info["metadata"].let { Metadata(it["soundFile"].str, it["duration"].double) },
                info["mouthCues"].list.map {
                    MouthCue(it["start"].double, it["end"].double, it["value"].str)
                }
            )
		} finally {
			tempFile.delete()
		}
	}

	data class Metadata(val soundFile: String, val duration: Double)
	data class MouthCue(val start: Double, var end: Double, var value: String)
	//data class MouthCue(val start: Double, var end: Double, var value: Char)

	data class RhubarbFile(val metadata: Metadata, val mouthCues: List<MouthCue>) {
		val totalTime: Double by lazy { mouthCues.map { it.end }.maxOrNull() ?: 0.0 }
		fun findCue(time: Double): MouthCue? =
			mouthCues.getOrNull(mouthCues.binarySearch { if (time < it.start) +1 else if (time >= it.end) -1 else 0 })

		fun toLipString(): String {
			var out = ""
			val totalMs = (totalTime * 1000).toInt()
			for (ms in 0 until totalMs step 16) {
				val s = findCue(ms.toDouble() / 1000)
				out += s?.value ?: "X"
			}
			return out
		}
	}
}
*/
