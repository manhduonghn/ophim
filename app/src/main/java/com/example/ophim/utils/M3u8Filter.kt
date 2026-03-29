package com.example.ophim.utils

import android.util.Base64

object M3u8Filter {

    fun processM3u8(rawContent: String, originalUrl: String): String {
        val lines = rawContent.lines()
        val result = mutableListOf<String>()
        
        // Lấy Base URL từ link gốc để nối vào các link segment tương đối
        val baseUrl = originalUrl.substringBeforeLast("/") + "/"

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) { i++; continue }

            // 1. Xử lý khối Discontinuity (Quảng cáo từ 5-20 segments)
            if (line == "#EXT-X-DISCONTINUITY") {
                var j = i + 1
                var segmentCount = 0
                while (j < lines.size && lines[j].trim() != "#EXT-X-DISCONTINUITY") {
                    if (lines[j].contains(".ts") || lines[j].contains("segment")) segmentCount++
                    j++
                }
                
                if (segmentCount in 5..20) {
                    i = j + 1 // Bỏ qua toàn bộ khối này
                    continue
                }
                i++ // Bỏ qua thẻ DISCONTINUITY hiện tại nhưng giữ nội dung bên dưới
                continue
            }

            // 2. Loại bỏ convertv7
            if (line.contains("convertv7")) {
                if (result.isNotEmpty() && result.last().startsWith("#EXTINF")) {
                    result.removeAt(result.size - 1)
                }
                i++; continue
            }

            // 3. Chuẩn hóa đường dẫn tuyệt đối
            when {
                !line.startsWith("#") -> {
                    // Nối domain nếu link là tương đối
                    result.add(if (line.startsWith("http")) line else baseUrl + line)
                }
                line.startsWith("#EXT-X-KEY") -> {
                    // Fix link Key giải mã (AES-128)
                    val fixedKey = if (line.contains("URI=\"http")) line 
                                  else line.replace("URI=\"", "URI=\"$baseUrl")
                    result.add(fixedKey)
                }
                line == "#EXT-X-DISCONTINUITY" -> { /* Skip hoàn toàn thẻ này */ }
                else -> result.add(line)
            }
            i++
        }
        return result.joinToString("\n")
    }

    fun createDataUri(content: String): String {
        val base64 = Base64.encodeToString(content.toByteArray(), Base64.NO_WRAP)
        return "data:application/vnd.apple.mpegurl;base64,$base64"
    }
}
