function signInCallback(authResult) {
    if (authResult['code']) {
        $('#signinButton').hide();
        $('.description').text("Processing login...");
        window.location = "/authenticate?code=" + authResult['code'];
    }
}
