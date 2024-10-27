
/**
 * @license
 * Copyright 2019 Google LLC. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
// [START maps_add_map]
// Initialize and add the map
let map;
let circles;
let currentUrl = window.location.protocol + "//" + window.location.hostname + ":" + window.location.port;

function clearMap() {
  for (let i = 0; i < 10; i++) {
    circles[i].setMap(null);
  }
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

  map.addListener("center_changed", () => {
    console.log(currentUrl)
    let bounds = map.getBounds();
    let fetchUrl = currentUrl + "/json-data/" + bounds.bi.lo + "/" + bounds.Gh.lo + "/" + bounds.bi.hi + "/" + bounds.Gh.hi;
    fetch(fetchUrl)
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok ' + response.statusText);
            }
            return response.json(); // Parse the response as JSON
        })
        .then(data => {
            clearMap();
            // Process the JSON data here
            console.log(data); // Example: print the JSON data
            for (let i = 0; i < Math.min(data.clusters.length, 10); i++) {
              let cluster = data.clusters[i]
              circles[i].setCenter({ lat: cluster.latitude, lng: cluster.longitude })
              circles[i].setMap(map);
            }
            // You can now manipulate the data as needed
        })
        .catch(error => {
            console.error('There was a problem with the fetch operation:', error);
        });
  });

  // Initialize 10 circles. Do not place them on the map yet.
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
                       radius: 6,
                     });
    circle.setMap(map);
    circles.push(circle);
  }
}

initMap();
// [END maps_add_map]
