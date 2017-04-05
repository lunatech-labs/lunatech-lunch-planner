function signInCallback(authResult) {
    if (authResult['code']) {
        $('#signinButton').hide();
        $('.description').text("Processing login...");
        var code = authResult['code'];
        var id_token = authResult['id_token'];
        var access_token = authResult['access_token'];
        window.location="/authenticate?code="+code+"&id_token="+id_token+"&access_token="+access_token;
    }
}
