// programSwitch.ts — emitted by bms-screen-modernize (only for apps whose programs
// share a BMS map name). Pure dispatch, no styling, no app coupling.
import { defineComponent, h } from 'vue'
import type { Component } from 'vue'

/**
 * Wrap a { programId → component } map into ONE component for the screenName-keyed
 * registry: it renders the entry matching the `program` prop.
 *
 * `inheritAttrs: false` + `h(target, attrs)` forwards the whole ScreenOverrideProps
 * object untouched — declaring the props here would remove them from `attrs`.
 * A program id that is not in the map falls back to the first entry: ScreenView keeps
 * `program` at the program the user opened while a CICS XCTL can move the session to
 * another one, so a miss must still render a screen rather than a blank pane.
 */
export function byProgram(map: Record<string, Component>): Component {
  const first = Object.values(map)[0]
  return defineComponent({
    name: 'ProgramSwitch',
    inheritAttrs: false,
    setup(_props, { attrs }) {
      return () => {
        const id = String((attrs as Record<string, unknown>).program ?? '').toLowerCase()
        return h(map[id] ?? first, attrs as Record<string, unknown>)
      }
    },
  })
}
