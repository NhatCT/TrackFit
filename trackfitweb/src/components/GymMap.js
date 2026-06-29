import React, { useState, useEffect, useRef, useContext } from "react";
import { Container, Row, Col, Card, Form, Button, Spinner, Alert } from "react-bootstrap";
import { MyUserContext } from "../configs/Context";
import AOS from "aos";
import "aos/dist/aos.css";

// Default fallback coordinates (Hoan Kiem Lake, Hanoi)
const DEFAULT_LAT = 21.0285;
const DEFAULT_LON = 105.8542;

const calculateDistance = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Earth radius in km
  const dLat = (lat2 - lat1) * (Math.PI / 180);
  const dLon = (lon2 - lon1) * (Math.PI / 180);
  const a =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * (Math.PI / 180)) *
      Math.cos(lat2 * (Math.PI / 180)) *
      Math.sin(dLon / 2) *
      Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c; // Distance in km
};

const GymMap = () => {
  const [user] = useContext(MyUserContext);
  const [leafletLoaded, setLeafletLoaded] = useState(false);
  const [userLoc, setUserLoc] = useState(null); // { lat, lon }
  const [radius, setRadius] = useState(3000); // 3km default
  const [gyms, setGyms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [locationStatus, setLocationStatus] = useState("Đang xác định vị trí...");

  const leafletInstanceRef = useRef(null);
  const markersGroupRef = useRef(null);
  const userMarkerRef = useRef(null);
  const circleRef = useRef(null);

  useEffect(() => {
    AOS.init({ duration: 800, once: true });
    
    // Dynamically load Leaflet assets if not loaded
    const loadLeaflet = async () => {
      if (window.L) {
        setLeafletLoaded(true);
        return;
      }

      // Add CSS
      const link = document.createElement("link");
      link.rel = "stylesheet";
      link.href = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.css";
      link.integrity = "sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY=";
      link.crossOrigin = "";
      document.head.appendChild(link);

      // Add JS
      const script = document.createElement("script");
      script.src = "https://unpkg.com/leaflet@1.9.4/dist/leaflet.js";
      script.integrity = "sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo=";
      script.crossOrigin = "";
      script.onload = () => setLeafletLoaded(true);
      script.onerror = () => setError("Không thể tải bản đồ. Vui lòng kiểm tra kết nối mạng.");
      document.body.appendChild(script);
    };

    loadLeaflet();
    getUserLocation();

    return () => {
      if (leafletInstanceRef.current) {
        leafletInstanceRef.current.remove();
        leafletInstanceRef.current = null;
      }
    };
  }, []);

  // Request browser geolocation
  const getUserLocation = () => {
    setLocationStatus("Đang xác định vị trí...");
    setError(null);

    if (!navigator.geolocation) {
      setUserLoc({ lat: DEFAULT_LAT, lon: DEFAULT_LON });
      setLocationStatus("Trình duyệt không hỗ trợ Geolocation. Đang dùng vị trí mặc định (Hà Nội).");
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setUserLoc({ lat: pos.coords.latitude, lon: pos.coords.longitude });
        setLocationStatus("Đã xác định vị trí của bạn.");
      },
      (err) => {
        console.warn("Geolocation error:", err);
        setUserLoc({ lat: DEFAULT_LAT, lon: DEFAULT_LON });
        setLocationStatus("Không thể lấy vị trí. Đang sử dụng vị trí mặc định (Hà Nội). Hãy nhấp vào bản đồ để chọn vị trí.");
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 }
    );
  };

  // Initialize and Update Map
  useEffect(() => {
    if (!leafletLoaded || !userLoc || !window.L) return;

    const L = window.L;

    // Custom Icons using SVG/emoji-based divIcon for style consistency and premium look
    const userIcon = L.divIcon({
      html: `<div class="map-marker user-marker"><div class="marker-pulse"></div>👤</div>`,
      className: "custom-leaflet-marker",
      iconSize: [36, 36],
      iconAnchor: [18, 18],
    });

    if (!leafletInstanceRef.current) {
      // Create map instance
      const map = L.map("map-container").setView([userLoc.lat, userLoc.lon], 14);
      leafletInstanceRef.current = map;

      // Dark Mode Tile Layer from CartoDB
      L.tileLayer("https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png", {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>',
        subdomains: "abcd",
        maxZoom: 20,
      }).addTo(map);

      // Create a layer group for gym markers
      markersGroupRef.current = L.layerGroup().addTo(map);

      // Add user marker
      userMarkerRef.current = L.marker([userLoc.lat, userLoc.lon], { icon: userIcon })
        .addTo(map)
        .bindPopup("<b>Vị trí của bạn</b>")
        .openPopup();

      // Add circle representing range
      circleRef.current = L.circle([userLoc.lat, userLoc.lon], {
        color: "#ff6b35",
        fillColor: "#ff6b35",
        fillOpacity: 0.1,
        radius: radius,
        weight: 1.5,
      }).addTo(map);

      // Map Click event - allow user to relocate the search center
      map.on("click", (e) => {
        const { lat, lng } = e.latlng;
        setUserLoc({ lat, lon: lng });
        setLocationStatus("Đã đổi vị trí tìm kiếm sang điểm chọn trên bản đồ.");
      });
    } else {
      const map = leafletInstanceRef.current;
      map.setView([userLoc.lat, userLoc.lon]);

      if (userMarkerRef.current) {
        userMarkerRef.current.setLatLng([userLoc.lat, userLoc.lon]);
      }

      if (circleRef.current) {
        circleRef.current.setLatLng([userLoc.lat, userLoc.lon]);
        circleRef.current.setRadius(radius);
      }
    }

    // Trigger Fetch Gyms whenever location or radius changes
    fetchGyms(userLoc.lat, userLoc.lon, radius);

  }, [leafletLoaded, userLoc, radius]);

  // Fetch Gyms using Overpass API
  const fetchGyms = async (lat, lon, r) => {
    setLoading(true);
    setError(null);
    
    const endpoints = [
      "https://overpass-api.de/api/interpreter",
      "https://lz4.overpass-api.de/api/interpreter",
      "https://overpass.kumi.systems/api/interpreter"
    ];
    
    const query = `[out:json][timeout:25];(node["leisure"="fitness_centre"](around:${r},${lat},${lon});node["amenity"="gym"](around:${r},${lat},${lon});way["leisure"="fitness_centre"](around:${r},${lat},${lon});way["amenity"="gym"](around:${r},${lat},${lon}););out center;`;
    
    let success = false;
    let data = null;
    let lastError = null;
    
    for (const url of endpoints) {
      try {
        console.log(`[Overpass] Querying ${url}...`);
        const response = await fetch(url, {
          method: "POST",
          body: query,
        });
        
        if (response.ok) {
          data = await response.json();
          success = true;
          console.log(`[Overpass] Success with ${url}`);
          break;
        } else {
          console.warn(`[Overpass] ${url} failed with status: ${response.status}`);
          lastError = `HTTP ${response.status}`;
        }
      } catch (err) {
        console.warn(`[Overpass] ${url} request failed:`, err);
        lastError = err.message || err;
      }
    }
    
    if (!success) {
      setError(`Không thể kết nối tới các dịch vụ bản đồ (Overpass API). Lỗi: ${lastError}. Vui lòng thử lại sau.`);
      setLoading(false);
      return;
    }

    try {
      // Parse results
      const results = (data.elements || []).map((el) => {
        // Ways in Overpass API have a "center" field when using out center
        const itemLat = el.lat || (el.center && el.center.lat);
        const itemLon = el.lon || (el.center && el.center.lon);
        const dist = calculateDistance(lat, lon, itemLat, itemLon);
        
        return {
          id: el.id,
          name: el.tags.name || "Phòng Gym/Fitness chưa đặt tên",
          lat: itemLat,
          lon: itemLon,
          distance: dist,
          address: el.tags["addr:full"] || 
                   (el.tags["addr:street"] ? `${el.tags["addr:housenumber"] || ""} ${el.tags["addr:street"]}`.trim() : null) || 
                   el.tags.address ||
                   "Chưa có địa chỉ chi tiết",
          openingHours: el.tags.opening_hours || null,
        };
      });

      // Sort gyms by distance
      results.sort((a, b) => a.distance - b.distance);
      setGyms(results);

      // Update Map Markers
      if (markersGroupRef.current && window.L) {
        markersGroupRef.current.clearLayers();
        const L = window.L;

        results.forEach((gym) => {
          if (!gym.lat || !gym.lon) return;

          const gymIcon = L.divIcon({
            html: `<div class="map-marker gym-marker">🏋️</div>`,
            className: "custom-leaflet-marker",
            iconSize: [32, 32],
            iconAnchor: [16, 16],
          });

          const popupContent = `
            <div class="map-popup-dark">
              <h6>${gym.name}</h6>
              <p class="mb-1 text-muted"><small>📍 ${gym.address}</small></p>
              <p class="mb-2 text-orange font-monospace"><b>📏 Khoảng cách: ${gym.distance.toFixed(2)} km</b></p>
              <a href="https://www.google.com/maps/dir/?api=1&destination=${gym.lat},${gym.lon}" target="_blank" class="btn btn-sm btn-orange text-white w-100 py-1">Chỉ đường</a>
            </div>
          `;

          L.marker([gym.lat, gym.lon], { icon: gymIcon })
            .bindPopup(popupContent)
            .addTo(markersGroupRef.current);
        });
      }

    } catch (err) {
      console.error("Overpass query error:", err);
      setError("Có lỗi xảy ra khi tìm kiếm phòng tập. Vui lòng thử lại.");
    } finally {
      setLoading(false);
    }
  };

  const focusGym = (gym) => {
    if (!leafletInstanceRef.current || !window.L) return;
    const map = leafletInstanceRef.current;
    
    // Zoom and pan to gym
    map.setView([gym.lat, gym.lon], 16);

    // Open marker popup. Find marker in group layer.
    if (markersGroupRef.current) {
      markersGroupRef.current.eachLayer((layer) => {
        const latlng = layer.getLatLng();
        if (latlng.lat === gym.lat && latlng.lng === gym.lon) {
          layer.openPopup();
        }
      });
    }
  };

  return (
    <Container className="py-4" data-aos="fade-up">
      {/* Dynamic Style Injection for Leaflet customization */}
      <style>{`
        .custom-leaflet-marker {
          background: transparent;
          border: none;
        }
        .map-marker {
          display: flex;
          align-items: center;
          justify-content: center;
          width: 100%;
          height: 100%;
          font-size: 1.25rem;
          border-radius: 50%;
          box-shadow: 0 4px 10px rgba(0,0,0,0.5);
          position: relative;
          color: white;
          transition: transform 0.2s ease-in-out;
        }
        .map-marker:hover {
          transform: scale(1.2);
        }
        .user-marker {
          background: #0d6efd;
          border: 2px solid white;
          z-index: 1000;
        }
        .marker-pulse {
          position: absolute;
          width: 100%;
          height: 100%;
          background: #0d6efd;
          border-radius: 50%;
          animation: pulse 1.8s infinite ease-in-out;
          z-index: -1;
          opacity: 0.6;
        }
        @keyframes pulse {
          0% { transform: scale(1); opacity: 0.6; }
          100% { transform: scale(2.2); opacity: 0; }
        }
        .gym-marker {
          background: #ff6b35;
          border: 2px solid white;
        }
        .map-popup-dark {
          font-family: inherit;
          color: #fff;
          min-width: 180px;
        }
        .map-popup-dark h6 {
          margin: 0 0 6px 0;
          color: #ff6b35;
          font-weight: 600;
          font-size: 0.95rem;
        }
        .map-popup-dark p {
          font-size: 0.8rem;
        }
        .btn-orange {
          background-color: #ff6b35;
          border-color: #ff6b35;
          transition: background-color 0.2s;
        }
        .btn-orange:hover {
          background-color: #e55a2b;
          border-color: #e55a2b;
        }
        .text-orange {
          color: #ff6b35;
        }
        .leaflet-popup-content-wrapper {
          background: #111a2b !important;
          border: 1px solid #1f2d47;
          border-radius: 8px;
          box-shadow: 0 6px 15px rgba(0,0,0,0.6);
        }
        .leaflet-popup-content {
          margin: 12px !important;
        }
        .leaflet-popup-tip {
          background: #111a2b !important;
          border: 1px solid #1f2d47;
        }
        .gym-list-container {
          max-height: 500px;
          overflow-y: auto;
          scrollbar-width: thin;
          scrollbar-color: #1f2d47 #111a2b;
        }
        .gym-list-container::-webkit-scrollbar {
          width: 6px;
        }
        .gym-list-container::-webkit-scrollbar-track {
          background: #111a2b;
        }
        .gym-list-container::-webkit-scrollbar-thumb {
          background-color: #1f2d47;
          border-radius: 3px;
        }
        .gym-item-card {
          background: #111a2b;
          border: 1px solid #1f2d47;
          border-radius: 8px;
          transition: all 0.2s ease-in-out;
          cursor: pointer;
        }
        .gym-item-card:hover {
          transform: translateY(-2px);
          border-color: #ff6b35;
          background: #152238;
        }
      `}</style>

      <Row className="mb-4 align-items-center">
        <Col md={7}>
          <h2 className="text-white mb-2">🗺️ Bản đồ Tìm Phòng Tập</h2>
          <p className="text-light-50 mb-0">
            Khám phá các phòng tập thể hình, gym, fitness xung quanh bạn sử dụng OpenStreetMap. 
            Nhấp chuột vào bất cứ đâu trên bản đồ để thay đổi tâm điểm tìm kiếm.
          </p>
        </Col>
        <Col md={5} className="mt-3 mt-md-0 text-md-end">
          <Button variant="outline-light" onClick={getUserLocation} className="gap-2 align-items-center d-inline-flex me-2">
            📍 Vị trí hiện tại
          </Button>
        </Col>
      </Row>

      {locationStatus && (
        <Alert variant="info" className="bg-surface text-light border-0 py-2 mb-3" style={{ fontSize: "0.9rem" }}>
          💡 {locationStatus}
        </Alert>
      )}

      {error && (
        <Alert variant="danger" className="d-flex justify-content-between align-items-center">
          <span>{error}</span>
          {error.includes("hội viên PRO") && (
            <Button size="sm" variant="warning" className="fw-bold text-dark text-nowrap ms-2" href="/upgrade">
              Nâng cấp PRO 👑
            </Button>
          )}
        </Alert>
      )}

      <Row className="mb-3">
        <Col md={4} className="mb-2 mb-md-0">
          <Form.Group className="d-flex gap-2 align-items-center">
            <Form.Label className="text-light mb-0 text-nowrap">Bán kính quét:</Form.Label>
            <Form.Select 
              value={radius} 
              onChange={(e) => {
                const val = Number(e.target.value);
                if (val > 1000 && !user?.isPremium) {
                  setError("Bán kính quét lớn hơn 1km chỉ dành cho hội viên PRO! Vui lòng nâng cấp tài khoản để sử dụng.");
                  setRadius(1000);
                } else {
                  setRadius(val);
                }
              }}
              style={{ background: "#111a2b", color: "#fff", borderColor: "#1f2d47" }}
            >
              <option value={1000}>1 km</option>
              <option value={3000}>3 km</option>
              <option value={5000}>5 km</option>
              <option value={10000}>10 km</option>
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>

      <Row>
        {/* Left Column: Gym List */}
        <Col lg={4} className="mb-4 mb-lg-0">
          <h5 className="text-white mb-3 d-flex justify-content-between align-items-center">
            <span>Danh sách gần bạn</span>
            {loading ? (
              <Spinner animation="border" size="sm" variant="warning" />
            ) : (
              <span className="badge bg-warning text-dark" style={{ fontSize: "0.8rem" }}>
                {gyms.length} kết quả
              </span>
            )}
          </h5>

          <div className="gym-list-container pe-1">
            {gyms.length === 0 && !loading && (
              <Card className="gym-item-card p-4 text-center text-light-50">
                🫙 Không tìm thấy phòng tập nào trong bán kính này. Hãy chọn bán kính lớn hơn.
              </Card>
            )}

            {gyms.map((gym) => (
              <Card 
                key={gym.id} 
                className="gym-item-card p-3 mb-2"
                onClick={() => focusGym(gym)}
              >
                <h6 className="text-white mb-1">{gym.name}</h6>
                <div className="text-light-50 small mb-2">📍 {gym.address}</div>
                {gym.openingHours && (
                  <div className="text-warning small mb-2" style={{ fontSize: "0.75rem" }}>
                    🕒 Mở cửa: {gym.openingHours}
                  </div>
                )}
                <div className="d-flex justify-content-between align-items-center mt-2">
                  <span className="text-orange font-monospace small fw-bold">
                    📏 {gym.distance < 1 ? `${(gym.distance * 1000).toFixed(0)} m` : `${gym.distance.toFixed(2)} km`}
                  </span>
                  <Button 
                    size="sm" 
                    variant="link" 
                    className="text-orange p-0 text-decoration-none"
                    href={`https://www.google.com/maps/dir/?api=1&destination=${gym.lat},${gym.lon}`}
                    target="_blank"
                    onClick={(e) => e.stopPropagation()}
                  >
                    Chỉ đường ↗
                  </Button>
                </div>
              </Card>
            ))}
          </div>
        </Col>

        {/* Right Column: Leaflet Map */}
        <Col lg={8}>
          <Card className="border-0 shadow-lg" style={{ background: "#111a2b", overflow: "hidden", borderRadius: "12px" }}>
            <div 
              id="map-container" 
              style={{ height: "500px", width: "100%", zIndex: 1 }}
            >
              {!leafletLoaded && (
                <div className="h-100 d-flex flex-column align-items-center justify-content-center text-light-50">
                  <Spinner animation="border" variant="warning" className="mb-2" />
                  Đang tải bản đồ...
                </div>
              )}
            </div>
          </Card>
        </Col>
      </Row>
    </Container>
  );
};

export default GymMap;
