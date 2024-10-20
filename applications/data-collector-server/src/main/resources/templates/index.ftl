<#import "template.ftl" as layout />

<@layout.noauthentication>
    <section>
        <div class="container">
            <p>
                The list of stored collision records.
            </p>
            <p>
                <#list data as item>
                    <p>${item}</p>
                </#list>
            </p>
        </div>
    </section>

</@layout.noauthentication>