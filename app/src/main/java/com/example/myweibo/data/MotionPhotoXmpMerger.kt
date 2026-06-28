package com.example.myweibo.data

/**
 * XMP merge helpers aligned with flutter_live_motion:
 * preserve existing APP1 metadata when possible, only refresh length fields.
 */
internal object MotionPhotoXmpMerger {
  private val XMP_PADDING = buildString {
    repeat(8) {
      append('\n')
      append(" ".repeat(256))
    }
  }

  fun mergeOrCreate(
    originalXmp: String?,
    videoLength: Int,
    presentationTimestampUs: Long,
  ): String {
    if (originalXmp.isNullOrBlank()) {
      return buildDefaultXmp(videoLength, presentationTimestampUs)
    }
    val updated = updateLengthFields(originalXmp, videoLength, presentationTimestampUs)
    return if (
      updated.contains("GCamera:MicroVideo=\"1\"") ||
      updated.contains("GCamera:MotionPhoto=\"1\"") ||
      updated.contains("MiCamera:")
    ) {
      updated
    } else {
      buildDefaultXmp(videoLength, presentationTimestampUs)
    }
  }

  private fun updateLengthFields(
    xmp: String,
    videoLength: Int,
    presentationTimestampUs: Long,
  ): String {
    val timestamp = presentationTimestampUs.toString()
    var updated = xmp
    updated = updated.replace(
      Regex("""OpCamera:VideoLength="\d+""""),
      """OpCamera:VideoLength="$videoLength"""",
    )
    updated = updated.replace(
      Regex("""GCamera:MicroVideoOffset="\d+""""),
      """GCamera:MicroVideoOffset="$videoLength"""",
    )
    updated = updated.replace(
      Regex("""(Item:Semantic="MotionPhoto"[\s\S]*?Item:Length=")(\d+)(")"""),
      "$1$videoLength$3",
    )
    updated = updated.replace(
      Regex("""GCamera:MotionPhotoPresentationTimestampUs="\d+""""),
      """GCamera:MotionPhotoPresentationTimestampUs="$timestamp"""",
    )
    updated = updated.replace(
      Regex("""GCamera:MicroVideoPresentationTimestampUs="\d+""""),
      """GCamera:MicroVideoPresentationTimestampUs="$timestamp"""",
    )
    updated = updated.replace(
      Regex("""Camera:MotionPhotoPresentationTimestampUs="\d+""""),
      """Camera:MotionPhotoPresentationTimestampUs="$timestamp"""",
    )
    return updated
  }

  private fun buildDefaultXmp(
    videoLength: Int,
    presentationTimestampUs: Long,
  ): String {
    val timestamp = presentationTimestampUs.toString()
    return """
      |<x:xmpmeta xmlns:x="adobe:ns:meta/" x:xmptk="Adobe XMP Core 5.1.0-jc003">
      |  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#">
      |    <rdf:Description rdf:about=""
      |        xmlns:Camera="http://ns.google.com/photos/1.0/camera/"
      |        xmlns:GCamera="http://ns.google.com/photos/1.0/camera/"
      |        xmlns:MiCamera="http://ns.xiaomi.com/photos/1.0/camera/"
      |        xmlns:Container="http://ns.google.com/photos/1.0/container/"
      |        xmlns:Item="http://ns.google.com/photos/1.0/container/item/"
      |      Camera:MotionPhoto="1"
      |      Camera:MotionPhotoVersion="1"
      |      Camera:MotionPhotoPresentationTimestampUs="$timestamp"
      |      GCamera:MotionPhoto="1"
      |      GCamera:MotionPhotoVersion="1"
      |      GCamera:MotionPhotoPresentationTimestampUs="$timestamp"
      |      GCamera:MicroVideoVersion="1"
      |      GCamera:MicroVideo="1"
      |      GCamera:MicroVideoOffset="$videoLength"
      |      GCamera:MicroVideoPresentationTimestampUs="$timestamp"
      |      MiCamera:XMPMeta="&lt;?xml version='1.0' encoding='UTF-8' standalone='yes' ?&gt;">
      |      <Container:Directory>
      |        <rdf:Seq>
      |          <rdf:li rdf:parseType="Resource">
      |            <Container:Item
      |              Item:Mime="image/jpeg"
      |              Item:Semantic="Primary"
      |              Item:Length="0"
      |              Item:Padding="0"/>
      |          </rdf:li>
      |          <rdf:li rdf:parseType="Resource">
      |            <Container:Item
      |              Item:Mime="video/mp4"
      |              Item:Semantic="MotionPhoto"
      |              Item:Length="$videoLength"
      |              Item:Padding="0"/>
      |          </rdf:li>
      |        </rdf:Seq>
      |      </Container:Directory>
      |    </rdf:Description>
      |  </rdf:RDF>
      |$XMP_PADDING</x:xmpmeta>
      """.trimMargin().trim()
  }
}
