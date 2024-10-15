<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <p>
                An example application using Kotlin and Ktor.
            </p>
            <p>
                Local URL ${variables["local_url"]}
            </p>
            <button type="button" onclick="sendGetRequest()">Click Me!</button>
            <p id="response">Response will be displayed here</p>
        </div>
    </section>

    <script>
        function sendGetRequest() {
            const requestUrl = "${variables["local_url"]}/ping";  // Fetch from the passed variables

            fetch(requestUrl)
                .then(response => response.text())
                .then(data => {
                    document.getElementById("response").innerText = data;
                })
                .catch(error => {
                    console.error("Error:", error);
                });
        }
    </script>

</@layout.noauthentication>