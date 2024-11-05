
/**
 * @license
 * Copyright 2019 Google LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
// [START maps_add_map]
// Initialize and add the map
let map;
let circle_data;
let circles;
let currentUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;
let infoWindow;

function clearMap() {
  for (let i = 0; i < 10; i++) {
    circles[i].setMap(null);
  }
}

function calculateRadius() {
  let zoom = map.getZoom();
  let scale = 156543.03392 * Math.cos(map.getCenter().lat() * Math.PI / 180) / Math.pow(2, zoom);
  return 20 * scale;
}

function updateClusters() {
    console.log(currentUrl)
    let bounds = map.getBounds();
    const ne = bounds.getNorthEast();
    const sw = bounds.getSouthWest();
    let fetchUrl = currentUrl + "/json-data/" + sw.lat() + "/" + sw.lng() + "/" + ne.lat() + "/" + ne.lng();
    fetch(fetchUrl)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json(); // Parse the response as JSON
        })
        .then(data => {
            clearMap();
            circle_data = data.clusters;
            // Process the JSON data here
            console.log(data); // Example: print the JSON data
            for (let i = 0; i < Math.min(data.clusters.length, 10); i++) {
              let cluster = data.clusters[i]
              circles[i].setCenter({ lat: cluster.latitude, lng: cluster.longitude })
              circles[i].setMap(map);
              circles[i].setRadius(calculateRadius());
            }
            // You can now manipulate the data as needed
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
}

async function initMap() {
  // [START maps_add_map_instantiate_map]
  // The location of central Kansas.
  const position = { lat: 38.336116093218386, lng: -99.84312923685623 };
  // Request needed libraries.
  //@ts-ignore
  const { Map } = await google.maps.importLibrary("maps");
  const { AdvancedMarkerElement } = await google.maps.importLibrary("marker");

  // The map, centered at Uluru
  map = new Map(document.getElementById("map"), {
    zoom: 4.8,
    center: position,
    mapId: "DEMO_MAP_ID",
  });

  map.addListener("zoom_changed", () => {
    updateClusters();
  });

  map.addListener("center_changed", () => {
    updateClusters();
  });

  infoWindow = new google.maps.InfoWindow();

  // Initialize 10 circles. Do not place them on the map yet.
  circle_data = [];
  circles = [];
  for (let i = 0; i < 10; i++) {
    let circle = new google.maps.Circle({
                       strokeColor: "#FF0000",
                       strokeOpacity: 0.8,
                       strokeWeight: 2,
                       fillColor: "#FF0000",
                       fillOpacity: 0.35,
                       map,
                       center: position,
                       radius: calculateRadius(),
                     });
    circle.setMap(map);
    // Add click event listener to the circle
    google.maps.event.addListener(circle, 'click', function(event) {
      // Set the content dynamically based on circle data
      const contentString = `
        <div>
          <strong>Circle Information</strong><br>
          Accidents: ${circle_data[i].collisions}<br>
        </div>
      `;

      // Update the InfoWindow content
      infoWindow.setContent(contentString);

      // Set the position of the InfoWindow to the click location
      infoWindow.setPosition(circle.getCenter());

      // Open the InfoWindow on the map
      infoWindow.open(map);
    });

    circles.push(circle);
  }
}

initMap();
// [END maps_add_map]
