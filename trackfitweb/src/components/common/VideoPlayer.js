  // src/components/common/VideoPlayer.js
  import { useEffect, useMemo, useRef } from "react";
  import Hls from "hls.js";

  /** utils */
  const toHttps = (u) => (!u ? "" : /^https?:\/\//i.test(u) ? u : `https://${u.replace(/^\/\//, "")}`);
  const ytId = (u) => {
    if (!u) return null;
    const url = toHttps(u);
    const m1 = url.match(/[?&]v=([A-Za-z0-9_-]{11})/);
    if (m1) return m1[1];
    const m2 = url.match(/youtu\.be\/([A-Za-z0-9_-]{11})/);
    if (m2) return m2[1];
    const m3 = url.match(/youtube\.com\/embed\/([A-Za-z0-9_-]{11})/);
    if (m3) return m3[1];
    return null;
  };

  const VideoPlayer = ({ url, width = "100%", height = "360px", controls = true, poster }) => {
    const safeUrl = toHttps(url);
    const isYouTube = useMemo(() => !!ytId(safeUrl), [safeUrl]);
    const isHls = useMemo(() => /\.m3u8($|\?)/i.test(safeUrl), [safeUrl]);

    if (!safeUrl) return <div style={{ width, height, background: "#000", borderRadius: 8 }} />;

    // 1) YouTube → iframe nocookie (ổn định, ít bị chặn)
    if (isYouTube) {
      const id = ytId(safeUrl);
      const src = `https://www.youtube-nocookie.com/embed/${id}?rel=0&modestbranding=1`;
      return (
        <div style={{ width, height, position: "relative", background: "#000", borderRadius: 8, overflow: "hidden" }}>
          <iframe
            key={id}
            title="YouTube player"
            src={src}
            width="100%"
            height="100%"
            frameBorder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowFullScreen
            style={{ display: "block" }}
          />
        </div>
      );
    }

    // 2) HLS → hls.js
    if (isHls) {
      return <HlsVideo url={safeUrl} width={width} height={height} poster={poster} controls={controls} />;
    }

    // 3) File/mp4 → <video>
    return (
      <video
        key={safeUrl}
        src={safeUrl}
        width={width}
        height={height}
        controls={controls}
        poster={poster}
        playsInline
        style={{ background: "#000", borderRadius: 8 }}
      />
    );
  };

  const HlsVideo = ({ url, width, height, poster, controls }) => {
    const ref = useRef(null);

    useEffect(() => {
      const video = ref.current;
      if (!video) return;
      if (video.canPlayType("application/vnd.apple.mpegurl")) {
        video.src = url; // Safari
      } else if (Hls.isSupported()) {
        const hls = new Hls({ maxBufferLength: 10 });
        hls.loadSource(url);
        hls.attachMedia(video);
        return () => hls.destroy();
      } else {
        video.src = url; // fallback
      }
    }, [url]);

    return (
      <video
        ref={ref}
        controls={controls}
        poster={poster}
        width={width}
        height={height}
        playsInline
        style={{ background: "#000", borderRadius: 8 }}
      />
    );
  };

  export default VideoPlayer;
