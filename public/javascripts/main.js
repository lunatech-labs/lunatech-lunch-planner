$(function(){
    $('#select-all').click(function(event) {
        if(this.checked) {
            // Iterate each checkbox
            $('.schedule:checkbox').each(function() {
                this.checked = true;
            });
        }
        else {
            $(':checkbox').each(function() {
                this.checked = false;
            });
        }
    });
});

$(function(){
    $('.input-daterange').datepicker({
        format: 'dd-mm-yyyy',
        todayBtn: true,
        todayHighlight: true
    });
});

$(function(defaultDateStart, defaultDateEnd){
    $(".daterange").change(function(){
        var dateStart = document.getElementById("dateStart").value;
        var dateEnd = document.getElementById("dateEnd").value;

        if(dateStart !== defaultDateStart || dateEnd !== defaultDateEnd) {
            $('#filterDateRange').submit();
        }
    });
});

$(document).ready(function() {
    allSelected();

    $('button.js-delete-confirmation').click(function() {
        return confirm("Are you sure you want to delete?");
    })
});

$(".schedule").change(function(){
    allSelected();
});

function allSelected(){
    var allSelected = $('.schedule:checked').length === $('.schedule').length;

    if(allSelected) {
        $('#select-all').each(function() {
            this.checked = true;
        });
    }
}

$(function() {
    $('#formAttendance').submit(function (eventObj) {
        $('.schedule:checked').each(function () {
            $(this).append('<input type="hidden" name="menuDate[]" value="' + this.parentNode.parentNode.cells.item(1).textContent.toString() + '" />');
        });
        return true;
    });
});
