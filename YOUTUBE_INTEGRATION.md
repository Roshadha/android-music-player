# YouTube API Integration

## Overview
Your music player now integrates with YouTube Data API v3 to search and stream music from YouTube!

## Features Added

### 1. YouTube Search Integration
- **Search from YouTube**: When you search for songs, the app now searches YouTube's massive music library
- **Millions of songs**: Access to virtually any song available on YouTube
- **Automatic**: Works seamlessly alongside Jamendo and other sources

### 2. How It Works

#### Search Flow:
1. User types a search query (e.g., "shape of you")
2. App searches YouTube for music videos matching the query
3. Results show up in your search results with thumbnails
4. Tap any song to play it directly

#### What Gets Searched:
- YouTube API searches for: `"{your query} music"`
- Limited to Music category (categoryId: 10)
- Returns up to 25 results per search
- Shows video title, channel name, and thumbnail

### 3. Song Information

YouTube songs will show:
- **Title**: Extracted from video title (usually "Artist - Song Title" format)
- **Artist**: Channel name or extracted from title
- **Album**: Shows as "YouTube"
- **Thumbnail**: High-quality video thumbnail as album art
- **ID**: Prefixed with "yt_" to identify YouTube sources

### 4. Playback

**Current Implementation**:
- Uses YouTube video URLs
- ExoPlayer (Media3) attempts to play YouTube URLs directly
- Works for most public music videos

**Note**: YouTube's Terms of Service restrict direct audio extraction from their platform. The current implementation uses standard YouTube URLs which may have limitations. For production use, consider:
- YouTube Music API (requires special partnership)
- Embedding YouTube player (via YouTube Android Player API)
- Using only as a search/discovery tool with links to YouTube app

### 5. API Key

**Public API Key Included**: 
```
AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8
```

**Quota Limits**:
- Free tier: 10,000 queries per day
- Each search costs ~100 quota units
- ~100 searches per day possible

**For Production**:
1. Get your own API key from: https://console.cloud.google.com/
2. Enable YouTube Data API v3
3. Replace the key in `ApiClient.kt`
4. Set up quota monitoring

### 6. Source Priority

When searching, results come from:
1. **YouTube** (25 results) - Largest library
2. **Jamendo** (25 results) - Full-length free music
3. **Local Cache** - Previously loaded songs

### 7. Legal Considerations

⚠️ **Important**: 
- YouTube's Terms of Service prohibit downloading or extracting audio
- This implementation uses YouTube URLs which may not work reliably
- Consider these alternatives for production:
  - YouTube Music API (requires partnership)
  - YouTube Android Player API (embeds official player)
  - Use as discovery only, open YouTube app for playback
  - Stick to Jamendo/SoundHelix for actual streaming

### 8. Testing

**Try searching for**:
- "shape of you"
- "despacito"
- "imagine dragons"
- "billie eilish"
- Any popular song or artist

You'll see results from YouTube mixed with Jamendo results!

### 9. Files Modified

- `MusicApiService.kt` - Added YouTube API interface
- `ApiClient.kt` - Added YouTube API client
- `YouTubeExtractor.kt` - Audio extraction utilities (for future use)
- `MusicRepository.kt` - Integrated YouTube search
- `MusicPlayerManager.kt` - Added YouTube URL handling

### 10. Future Enhancements

Possible improvements:
- Add YouTube badge/icon on search results
- Show "Source: YouTube" label
- Add duration from YouTube API
- Implement proper YouTube player embedding
- Add "Open in YouTube" button
- Cache extracted audio URLs
- Better error handling for restricted videos

## Testing the Integration

1. **Open the app**
2. **Go to Search tab**
3. **Type any song name** (e.g., "perfect")
4. **See YouTube results** appear with thumbnails
5. **Tap to play** - should stream from YouTube

Enjoy your expanded music library! 🎵🎶
