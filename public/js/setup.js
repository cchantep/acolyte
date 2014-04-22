(function($) {
    $('.has-tooltip').one('mouseover', rt).tooltip()

    var ar = $('a[href="#add-rule"]'),
    notEmpty = function(e) { return ($.trim(e.val()) == "") ? false : true },
    isNum = function(e) { return $.isNumeric(e.val()) },
    rt = function(){
        ar.tooltip('destroy').tooltip({
            'title': "Define a case by adding a rule", 'placement': "bottom"
        })
    }, 
    getval = function(e) { return function() { return e.val() } },
    rl = $("#case-rules ol"),
    rd = $("#route-editor"), rf = $("#rule-form"), rsf = $("#result-form"),
    pc = $("#param-pattern .badge"), pp = $("#param-pattern .position"),
    pl = $("#param-pattern ol"),
    pt = $("#param-type"),
    rk = $("#ruleKind"), 
    adp = $("#add-param"), 
    re = $("#ruleExp").on('keyup change', function() {
        var ev = re.val();

        if (ev == "") {
            re.addClass("has-error"); 
            return adp.attr("disabled", "disabled")
        } 

        // ---

        try {
            new RegExp(ev)

            re.removeClass("has-error"); 
            adp.removeAttr("disabled")
        } catch (e) {
            re.addClass("has-error"); 
            adp.attr("disabled", "disabled")
        }
    }),
    raiserr = $("#raise-error"), rer = $("#result-error"), 
    red = $("#result-data"),
    applyRoute = $("#route-editor .ac-apply").click(function(){
        var ps = [], res = null, typ = rk.val();

        $("#param-pattern li").each(function(i,e){ 
            var p = $(e);
            ps.push({'_type':p.data('type'),'value':p.data('value')})
        });

        if (raiserr.is(":checked")) {
            res = {'error':rer.val()}
        } else if (typ == "update") {
            res = {'updateCount':$("#update-count").val()}
        } else {
            res = {'rows':[]};

            $("#result-data .res-row").each(function(i,e){
                var row = [];

                $("td", e).each(function(j,x){ 
                    if (j == 0) return true; 

                    var r = $(x);

                    row.push({'_type':r.data("type"),'value':r.text()})
                });

                res.rows.push(row)
            })
        }

        var rule = { '_type': typ, 'pattern': re.val(), 'result': res };
            
        console.debug("--> " + rule.toSource());
    }),
    addUpEd = function() {
        var uc = $('<input type="number" min="0" disabled="disabled" id="update-count" class="form-control" />'),
        hasC = function() {
            if (isNum(uc)) applyRoute.removeAttr("disabled");
            else applyRoute.attr("disabled", "disabled")
        },
        cf = function() {
            var re = $(this);

            if (re.is(":checked")) {
                uc.removeAttr("disabled");
                hasC()
            } else uc.attr("disabled", "disabled")
        };

        $('<div class="input-group"></div>').append(uc.on('keyup change', hasC)).prepend($('<label class="input-group-addon"> Update count</label>').prepend($('<input type="radio" name="result-type" id="update-success"/>').on('keyup change', cf))).tooltip({'title':"Execution is successful, then count of updated rows is returned."}).appendTo(red.empty())
    },
    addRwEd = function() {
        var qs = $('<input type="radio" name="result-type" id="query-success"/>'),
        uc = $('<select id="row-size" class="form-control"><option value="1">1 column</option><option value="2">2 columns</option><option value="3">3 columns</option></select>').tooltip({'title':"How many columns there will be on each result row?",'trigger':"focus",'delay':{'hide':3000}}).click(function(){ qs.trigger('click') }),
        nre = $('<div></div>').
            appendTo(red.empty()).css({'display':"none"}),
        rst = function() {
            var j, gct = [], cc = parseInt(uc.val()), 
            cl = (cc == 3) ? "col-md-4"
                : (cc == 2) ? "col-md-6"
                : "col-md-12",
            crb = $('<button class="btn btn-default">Create rows...</button>'),
            vdv = function(e) { return function() { return notEmpty(e) } },
            vnv = function(e) { return function() { return isNum(e) } };

            nre.empty().css({'display':"block"});

            for (j = 1; j <= cc; j++) {
                var hs = $('<select id="col' + j + 
                           'type" class="form-control"></select>');

                $.each(pts, function(v, t) {
                    hs.append('<option value="'+v+'">'+t+'</option>')
                });

                gct.push(getval(hs));

                $('<div class="form-group"></div>').
                    append('<label class="control-label" for="col' + j +
                           'type">Column #'+j+'</label>').
                    append(hs).appendTo(nre).
                    tooltip({'title':"Type of column #"+j})
            }

            nre.append(crb.click(function(){
                var t = $('<table class="table table-striped"></table>'),
                th = $('<tr><td></td></tr>').
                    appendTo($('<thead></thead>').appendTo(t)),
                tbe = $('<tr><td class="text-center"><i class="fa fa-pencil text-muted"></i></td></tr>').appendTo($('<tbody></tbody>').appendTo(t)),
                rab = $('<button class="btn btn-default" disabled="disabled">Add this row <i class="fa fa-hand-o-up"></i></button>'),
                rav = function(f) {
                    return function() {
                        var e = $(this);

                        if (!f(e)) e.addClass("has-error"); 
                        else e.removeClass("has-error")
                    }
                },
                cvf = [], rv = function() {
                    var ok = true;
                    for (j = 0; ok && j < cvf.length; j++) ok = cvf[j]();

                    if (ok) rab.removeAttr("disabled");
                    else rab.attr("disabled", "disabled")
                },
                gcv = [], gci = function(t, e) {
                    return function() {
                        var v = e.val();
                        e.val(null);
                        return {'_type':t, 'val':v}
                    }
                }, 
                rmrow = function(){ 
                    $(this).remove();

                    if ($(".res-row", t).length == 0) {
                        applyRoute.attr("disabled", "disabled")
                    }
                },
                addrow = function() {
                    var nr = $('<tr class="res-row"></tr>'),
                    ri = $('<i class="fa fa-minus-circle"></i>').
                        appendTo($('<td></td>').appendTo(nr)).
                        css({'display':"none"});

                    for (j = 0; j < gcv.length; j++) {
                        var col = gcv[j]();

                        $('<td>'+col.val+'</td>').
                            data("type", col._type).appendTo(nr)

                    }

                    nr.insertBefore(tbe).hover(function() {
                        ri.css({'display':"inline"})
                    }, function() { ri.css({'display':"none"}) }).
                        click(rmrow);

                    applyRoute.removeAttr("disabled");

                    return false
                };

                for (j = 1; j <= cc; j++) {
                    var ct = gct[j-1](),
                    ce = $('<input type="text" class="form-control" />');

                    th.append('<td class="text-center">Column #'+j+'</td>');

                    ce.appendTo($('<td></td>').appendTo(tbe));

                    if (ct == "date") {
                        cvf.push(vdv(ce));
                        ce.attr("readonly", "readonly").
                            datepicker({'format':"yyyy-mm-dd"}).
                            on('changeDate', rv).
                            tooltip({'title':"Date for column #"+j})

                    } else if (ct == "float") {
                        cvf.push(vnv(ce));
                        ce.on('keyup change', rav(isNum)).
                            on('keyup change', rv).
                            tooltip({'title':"Number for column #"+j})

                    } else {
                        ce.on('keyup change', rv).
                            tooltip({'title':"Text for column #"+j})
                    }

                    gcv.push(gci(ct, ce))
                }

                rab.appendTo(nre.empty()).click(addrow);
                nre.prepend(t);

                return false
            }))
        },
        cf = function() {
            raiserr.one('click', function() {
                uc.attr("disabled", "disabled");
                nre.empty()
            });

            applyRoute.attr("disabled", "disabled");
            uc.removeAttr("disabled");
            rst()
        };

        $('<div class="input-group"></div>').append(uc.change(rst)).prepend($('<label class="input-group-addon"> Result set</label>').tooltip({'title':"Execution is successful, then some rows are returned."}).prepend(qs.click(cf))).prependTo(red.append('<p class="text-muted">Column number is limited on purpose for this tour.</p>'));

        $('<p class="text-muted">Acolyte <a href="http://acolyte.eu.org/studio.html" rel="external me" title="Acolyte Studio">Studio</a> allows to re-use data from existing database.</p>').prependTo(red)

    },
    nxb = $("#route-editor .ac-next").click(function() {
        if (rk.val() == "update") addUpEd();
        else addRwEd();

        var e = $(this);

        rf.fadeOut(500, function() { 
            rsf.fadeIn(500, function() { 
                e.css({'display':"none"});
                applyRoute.css({'display':"inline"})
            })
        })
    }),
    pts = { 'string': "Text", 'float': "Number", 'date': "Date" },
    rmp = function() {
        var c = parseInt(pc.text());

        $(this).remove();

        if (c == 1) adp.attr("disabled", "disabled");

        pp.text(c); pc.text(c-1)
    },
    vf = function(f) {
        return function() {
            var e = $(this);
            if (!f(e)) { 
                e.addClass("has-error"); 
                adp.attr("disabled", "disabled")
            } else { e.removeClass("has-error"); adp.removeAttr("disabled") }
        }
    },
    pvs = vf(notEmpty),
    pvi = vf(isNum),
    pvt = {'title':"Expected value parameter",'trigger':"focus"},
    pvr = function() {
        var pv = $('<input type="text" class="form-control" id="param-value" />').tooltip(pvt).on('keyup change', pvs);
        
        $("#param-value").replaceWith(pv)
    };

    adp.click(function() {
        var n = parseInt(pp.text()), t = pt.val(), 
        v = $("#param-value").val(),
        i = $('<i class="fa fa-minus-circle"></i>');

        $('<li>' + pts[t] + ' = <tt class="text-info">' + v + '</tt></li>').prepend(i).
            data("type", t).data("value", v).appendTo(pl).
            hover(function() { i.css({'visibility':"visible"}) },
                  function() { i.css({'visibility':"hidden"}) }).
            click(rmp);

        adp.removeAttr("disabled");

        pp.text(n+1); pc.text(n);

        return false
    });

    raiserr.click(function(){
        if (notEmpty(rer)) applyRoute.removeAttr("disabled");
        else applyRoute.attr("disabled", "disabled")
    });

    rer.on('keyup change', function(){
        if (raiserr.is(":checked") && notEmpty($(this))) {
            applyRoute.removeAttr("disabled")
        } else applyRoute.attr("disabled", "disabled");
    });

    $.each(pts, function(v, t) {
        pt.append('<option value="'+v+'">'+t+'</option>')
    });

    ar.one('mouseover', rt).tooltip({
        'title': "First, add (at least) a rule to define which access case(s) you want to handle.",
        'placement': "bottom",
    }).on('hidden.bs.tooltip', rt).
        one('shown.bs.tooltip', function(){ setTimeout(rt, 15000) }).
        tooltip('show').
        click(function(){ 
            rf.trigger('reset').css({'display':"block"});
            rsf.trigger('reset').css({'display':"none"});
            nxb.css({'display':"inline"});
            applyRoute.css({'display':"none"});
            red.empty();
            pl.empty();
            pp.text(1);
            pc.text(0);
            rd.modal();
            return false
        });

    rf.on('reset', pvr);

    pt.change(function() {
        var v = $(this).val();

        adp.attr("disabled", "disabled");

        if (v == "date") {
            $("#param-value").replaceWith($('<input type="text" class="form-control ac-date" id="param-value" readonly="readonly" />').tooltip(pvt).datepicker({'format':"yyyy-mm-dd"}).one('changeDate', function() { adp.removeAttr("disabled") }))

        } else if (v == "float") {
            $("#param-value").replaceWith($('<input type="text" class="form-control" id="param-value" />').tooltip(pvt).on('keyup change', pvi))

        } else pvr();
    });

    $("#raise-error").change(function(){
        var e = $(this);
        if (e.is(":checked")) rer.removeAttr("disabled")
        else rer.attr("disabled", "disabled")
    });
})(jQuery);
