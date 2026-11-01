package com.hexandcube.sharelinkfx

data class PlatformConfig(
    val id: String,
    val name: String,
    val sourceDomains: List<String>,
    val defaultTarget: String,
    val suggestions: List<String>
)

object ServiceConfig {
    val platforms = listOf(
        PlatformConfig(
            "twitter",
            "Twitter / X",
            listOf("x.com", "twitter.com"),
            "fxtwitter.com",
            listOf("fxtwitter.com", "fixupx.com", "vxtwitter.com")
        ),
        PlatformConfig(
            "bluesky",
            "Bluesky",
            listOf("bsky.app"),
            "fxbsky.app",
            listOf("fxbsky.app")
        ),
        PlatformConfig(
            "instagram",
            "Instagram",
            listOf("instagram.com", "www.instagramfix.com"),
            "instagramfix.com",
            listOf("instagramfix.com", "c.instagramfix.com")
        ),
        PlatformConfig(
            "tiktok",
            "TikTok",
            listOf("tiktok.com", "www.tiktok.com"),
            "tnktok.com",
            listOf("tnktok.com")
        ),
        PlatformConfig(
            "tumblr",
            "Tumblr",
            listOf("tumblr.com", "www.tumblr.com"),
            "tpmblr.com",
            listOf("tpmblr.com")
        )
    )

    const val MASTODON_DEFAULT_PREFIX = "fxmas.to"
    val mastodonSuggestions = listOf("fxmas.to")
}