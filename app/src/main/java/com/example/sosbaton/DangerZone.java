package com.example.sosbaton;
import com.google.android.gms.maps.model.LatLng;
public class DangerZone {

        public LatLng center;   // 危険ピンの中心
        public double radius;   // 半径（m）

        public DangerZone(LatLng center, double radius) {
            this.center = center;
            this.radius = radius;
        }

}
