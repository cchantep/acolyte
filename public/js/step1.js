(function($) {
    $('.has-tooltip').one('mouseover', rt).tooltip()

    var ar = $('a[href="#add-rule"]'),
    rt = function(){
        ar.tooltip('destroy').tooltip({
            'title': "Define a case by adding a rule", 'placement': "bottom"
        })
    }, 
    rf = $("#rule-editor form"),
    pc = $("#param-pattern .badge"),
    pvc = function() {
        var e = $(this), b = $("#param-pattern .btn");

        if ($.trim($(e).val()) == "") b.attr("disabled", "disabled");
        else b.removeAttr("disabled")
    };

    ar.one('mouseover', rt).tooltip({
        'title': "First, add (at least) a rule to define which access case(s) you want to handle.",
        'placement': "bottom",
        'delay': {'hide':15000}
    }).on('hidden.bs.tooltip', rt).tooltip('show').
        click(function(){ 
            rf.trigger('reset'); 
            $("#rule-editor").modal();
            return false
        });

    rf.on('reset', function() {
        var pv = $('<input type="text" class="form-control" id="param-value" />').tooltip({'title':"Expected value parameter",'trigger':"focus"}).on('keyup change', pvc);

        $("#param-value").replaceWith(pv)
    })

})(jQuery);
