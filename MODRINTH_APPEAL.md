# Draft message to Modrinth support/moderation

Send this via Modrinth's support channel (their Discord `#support`/moderation-appeal
channel, or support@modrinth.com if you prefer email — check their current process, it
changes occasionally). Edit the bracketed parts before sending. Keep it short and factual
— don't over-explain or get defensive, the goal is just to ask a clear yes/no question.

---

> Subject: Question about a rejected project — Create FPS Boost
>
> Hi, my project "Create FPS Boost" (client-side NeoForge mod) was reviewed and rejected
> [link to the rejection thread/notification if you have one]. The reviewer said several
> described features either already exist in the game or don't effectively optimize
> anything, and asked that I not resubmit.
>
> I looked into this against the decompiled game source and the reviewer was right about
> some of it — a couple of features I described were redundant with vanilla behavior at
> default settings (e.g. vanilla already culls block entities via
> `BlockEntityRenderer.shouldRender()`, and already culls small entities by hitbox size
> via `Entity.shouldRenderAtSqrDistance()`). I've since:
>
> - Removed entity types from the default config where vanilla's own culling already
>   beats anything my mod could add (dropped items, snowballs, eggs, ender pearls,
>   potions, XP bottles)
> - Rewritten the description to be explicit about what's genuinely new (a global
>   particle budget vanilla doesn't have) vs. what automates/tightens existing vanilla
>   behavior (render distance / entity distance sliders, block-entity view distances)
>   rather than presenting automation as new capability
> - Removed inaccurate claims like "pixel-identical" and an overstated "near-to-far"
>   ordering guarantee
>
> Given the explicit request not to resubmit, I wanted to ask directly rather than just
> submit again: would a corrected version along these lines be considered, or is this
> project not something Modrinth wants regardless of the specific claims made? Happy to
> share the updated source (already public at
> https://github.com/levisnakes/create-fps-boost) or the exact diff if useful.
>
> Thanks for the review either way — happy to drop this entirely if that's the answer.

---

## Notes for you before sending

- Fill in the rejection thread/notification link if you have one — it helps them find
  the original review without digging.
- If they say no, respect that — don't resubmit under a new name/slug to route around it.
  GitHub distribution (already live) works fine without Modrinth.
- If they say yes, come back and I'll walk through [PUBLISHING.md](PUBLISHING.md) with you.
