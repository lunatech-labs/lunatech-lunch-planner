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
