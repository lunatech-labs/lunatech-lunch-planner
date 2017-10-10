$(function () {
    $(document).ready(function () {
        var selected = window.location.pathname;
        if (selected == "/") {
            $("#planned").addClass('selected');
        } else if (selected.startsWith("/dish/")) {
            $("#dishes").addClass('selected');
        } else if (selected.startsWith("/menu/")) {
            $("#menus").addClass('selected');
        } else if (selected.startsWith("/menuPerDay/")) {
            $("#schedule").addClass('selected');
        } else if (selected.startsWith("/profile")) {
            $("#profile").addClass('selected');
        }
    });
});
