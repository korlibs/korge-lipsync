import korlibs.time.milliseconds
import korlibs.korge.input.onClick
import korlibs.korge.lipsync.LipSyncEvent
import korlibs.korge.lipsync.readVoice
import korlibs.korge.scene.Scene
import korlibs.korge.view.SContainer
import korlibs.korge.view.image
import korlibs.korge.view.position
import korlibs.image.atlas.MutableAtlasUnit
import korlibs.image.atlas.readAtlas
import korlibs.image.format.readBitmap
import korlibs.image.format.readBitmapSlice
import korlibs.io.async.launchImmediately
import korlibs.io.file.std.resourcesVfs

class MainLipSyncScene : Scene() {
    override suspend fun SContainer.sceneMain() {
        val atlas = MutableAtlasUnit()
        val lipsByChar = "ABCDEFGHX".associate { it to resourcesVfs["lips/lisa-$it.png"].readBitmapSlice(atlas = atlas) }
        val lips = image(lipsByChar['A']!!)
        val lips2 = image(lipsByChar['A']!!).position(400, 0)
        onEvent(LipSyncEvent) {
            println(it)
            if (it.name == "lisa") {
                lips2.bitmap = lipsByChar[it.lip]!!
            }
        }
        var playing = true
        fun play() = launchImmediately {
            fun handler(event: LipSyncEvent) {
                views.dispatch(event)
                lips.bitmap = lipsByChar[event.lip]!!
                playing = event.time > 0.milliseconds
            }

            resourcesVfs["001.voice.wav"].readVoice().play("lisa") { handler(it) }
            //resourcesVfs["002.voice.wav"].readVoice().play("lisa") { handler(it) }
            //resourcesVfs["003.voice.wav"].readVoice().play("lisa") { handler(it) }
            //resourcesVfs["004.voice.wav"].readVoice().play("lisa") { handler(it) }
            //resourcesVfs["simple.voice.mp3"].readVoice().play("lisa") { handler(it) }
        }

        onClick {
            if (!playing) play()
        }
        play()
    }
}
