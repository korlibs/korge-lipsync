package korlibs.korge.lipsync

import korlibs.audio.sound.Sound
import korlibs.audio.sound.readSound
import korlibs.datastructure.Extra
import korlibs.datastructure.getExtra
import korlibs.datastructure.iterators.fastForEach
import korlibs.event.EventType
import korlibs.event.TypedEvent
import korlibs.io.file.VfsFile
import korlibs.korge.view.BaseView
import korlibs.korge.view.View
import korlibs.korge.view.Views
import korlibs.korge.view.animation.PlayableWithName
import korlibs.korge.view.descendantsOfType
import korlibs.time.TimeSpan
import korlibs.time.milliseconds
import korlibs.time.seconds

class LipSync(val lipsync: String) {
	val totalTime: TimeSpan get() = (lipsync.length * 16).milliseconds
	operator fun get(time: TimeSpan): Char = lipsync.getOrElse(time.millisecondsInt / 16) { 'X' }
	fun getAF(time: TimeSpan): Char {
		val c = this[time]
		return when (c) {
			'G' -> 'B'
			'H' -> 'C'
			'X' -> 'A'
			else -> c
		}
	}
}

//e: java.lang.UnsupportedOperationException: Class literal annotation arguments are not yet supported: Factory
//@AsyncFactoryClass(Voice.Factory::class)
class Voice(val voice: Sound, val lipsync: LipSync) {
	val totalTime: TimeSpan get() = lipsync.totalTime
	operator fun get(time: TimeSpan): Char = lipsync[time]
	fun getAF(time: TimeSpan): Char = lipsync.getAF(time)
	val event = LipSyncEvent()

	suspend fun play(name: String, views: Views) {
		play(name) { e ->
			views.dispatch(e)
		}
	}

	suspend fun play(name: String, handler: (LipSyncEvent) -> Unit) {
		voice.playAndWait { current, total ->
			if (current >= total) {
				handler(event.set(name, 0.seconds, 'X'))
			} else {
				handler(event.set(name, current, lipsync[current]))
			}
		}
	}
}

data class LipSyncEvent(var name: String = "", var time: TimeSpan = 0.seconds, var lip: Char = 'X') : TypedEvent<LipSyncEvent>(LipSyncEvent) {
	companion object : EventType<LipSyncEvent>

	fun set(name: String, elapsedTime: TimeSpan, lip: Char) = apply {
		this.name = name
		this.time = elapsedTime
		this.lip = lip
	}

	val timeMs: Int get() = time.millisecondsInt
}

class LipSyncComponent(val view: BaseView) {
	init {
		view.onEvent(LipSyncEvent) { event ->
			val name = view.getExtra("lipsync")
			if (event.name != name) return@onEvent
			(view as View?)?.descendantsOfType<PlayableWithName>()?.fastForEach {
				it.play("${event.lip}")
			}
		}
	}
}

val View.lipsync by Extra.PropertyThis { LipSyncComponent(this) }

suspend fun VfsFile.readVoice(): Voice {
	val lipsyncFile = this.withExtension("lipsync")
	return Voice(
		this.readSound(),
		LipSync(if (lipsyncFile.exists()) lipsyncFile.readString().trim() else "")
	)
}
