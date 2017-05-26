$(function() {
$(document).ready(function(){
var selected=window.location.pathname;
console.log(selected);
if(selected=="/")
$("#planned").addClass('selected');
else if(selected=="/dish/all")
$("#dishes").addClass('selected');
else if(selected=="/menu/all")
$("#menus").addClass('selected');
else if(selected=="/menuPerDay/all")
$("#schedule").addClass('selected');
else if(selected=="/profile")
$("#profile").addClass('selected');
});
});
