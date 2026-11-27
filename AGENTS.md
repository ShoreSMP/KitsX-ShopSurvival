# ShoreSMP player-facing style

Use [shore-style](https://github.com/ShoreSMP/shore-style) for all player-facing text and default configs, including strings rendered from code. Read its `SKILL.md` and consult the feature registry and templates before changing chat, menus, lore, broadcasts, titles or sidebars. Preserve existing config keys, placeholder names and supported formatting syntax.

From a checkout of this project, with shore-style cloned alongside it, check the shipped defaults with:

```sh
python ../shore-style/tools/shore_style_lint.py --strict --no-info src
```

Fix errors and warnings. Review menus and messages against the guide as well, since the lint only recognizes some config layouts.
