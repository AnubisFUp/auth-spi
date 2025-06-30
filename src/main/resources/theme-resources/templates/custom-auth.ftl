<#import "template.ftl" as layout>
<@layout.registrationLayout; section>
    <#if section = "header">
        Вход с пользовательскими учетными данными
    <#elseif section = "form">
        <form id="kc-custom-auth-form" action="${url.loginAction}" method="post">
            <div>
                <label for="username">Имя пользователя</label>
                <input type="text" id="username" name="username" required />
            </div>
            <div>
                <label for="password">Пароль</label>
                <input type="password" id="password" name="password" required />
            </div>
            <#if message?has_content>
                <div class="alert alert-${message.type}">
                    ${message.summary}
                </div>
            </#if>
            <div>
                <input type="submit" value="Войти" />
            </div>
        </form>
    </#if>
</@layout.registrationLayout>
