<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <p>
                An example application using Kotlin and Ktor.
            </p>
            <p>
                <#list data as item>${item}</#list>
            </p>
        </div>
    </section>

</@layout.noauthentication>