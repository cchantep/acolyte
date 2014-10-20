(function($) {
    var ar = $('#add-rule'),
    notEmpty = function(e) { return ($.trim(e.val()) == "") ? false : true },
    isNum = function(e) { return $.isNumeric(e.val()) },
    rt = function(){
        ar.tooltip('destroy').tooltip({
            'title': "Define a case by adding a rule", 'placement': "bottom"
        })
    }, 
    colDef = function(n, t) { 
        return function() { return {'name':n.val(), '_type':t.val()} }
    },
    rl = $("#case-rules .list-group"),
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
    raiserr = $("#raise-error"), 
    rer = $("#result-error").click(function(){
        raiserr.trigger('click'); return false
    }),
    red = $("#result-data"),
    testBut = $("#test-routes").click(function(){
        var fr = $('<form action="run.html" method="POST"></form>'),
        ar = new Array();

        $('.list-group-item', rl).each(function(i,e){
            ar.push($.parseJSON($(e).data('json')))
        });

        fr.append('<textarea name="json">'+JSON.stringify(ar)+'</textarea>').
            css({'visibility':"hidden"}).appendTo('body').trigger('submit');

        return false
    }),
    rmRouteHov = function(e){
        $('.operations', e).html('<i class="fa fa-minus-circle"></i>')
    },
    rmRoute = function(e){
        $('#case-result .list-group-item:nth('+e.index()+')').remove();
        e.remove();

        if ($('.list-group-item', rl).length == 0) {
            testBut.attr("disabled", "disabled").
                text("No route to prepare connection")
        }
    },
    routeUp = function(e){
        var res = $('#case-result .list-group-item:nth('+e.index()+')'),
        pres = res.prev(), pr = e.prev();

        res.removeClass("hover").detach().insertBefore(pres);
        $('.operations', e).html(' ');
        e.detach().insertBefore(pr)
    },
    routeDown = function(e){
        var res = $('#case-result .list-group-item:nth('+e.index()+')'),
        nres = res.next(), nr = e.next();

        res.removeClass("hover").detach().insertAfter(nres);
        $('.operations', e).html(' ');
        e.detach().insertAfter(nr);
    },
    mvRouteHov = function(e){
        var up = $('<i class="fa fa-long-arrow-up"></i>'),
        down = $('<i class="fa fa-long-arrow-down"></i>'),
        x = e.index();

        if (x == 0) up.addClass("text-muted");
        else up.css({'cursor':"n-resize"}).
            click(function(){ routeUp(e); return false });

        if (e.next().length == 0) down.addClass("text-muted");
        else down.css({'cursor':"s-resize"}).
            click(function(){ routeDown(e); return false });

        $('.operations', e).empty().append(up).append(down)
    },
    routeHov = rmRouteHov,
    routeOp = rmRoute,
    moveRoute = $("#move-rule"),
    loadRoute = function(typ,pat,res){
        // pat: Pattern = { 'expression': string, 'parameters': [{'_type': ..., 'value', ...}, ...]}

        var restxt = null, resovr = null;

        if (res['error']) {
            restxt = 'Error = <tt class="text-info">' + res.error + "</tt>"
        } else if (typ == "update") {
            var c = res['updateCount'];
            restxt = "Updated row" + (c>1?"s":"") + 
                ': <tt class="text-info">' + c + '</tt>'
        } else {
            var cts = "", ph = "", i;

            for (i = 0; i < res.schema.length; i++) {
                var ct = res.schema[i]._type, 
                cn = res.schema[i].name;

                if (cts != "") cts += ", ";
                cts += ct;

                ph += "<td>"+cn+"</td>"
            }

            restxt = "Result set = " + res.rows.length + " x (" + cts + ')';
            resovr = { 
                'placement': "top", 'trigger': "hover", 'html': true,
                'content': '<table class="table table-striped"><thead><tr>'+ph+'</tr></thead><tbody>' + $('#result-data tbody').html() + '</tbody></table>'
            }
        }

        var rul = $('<a class="list-group-item" href="#">' + typ + ' = <tt class="text-info">' + pat.expression + '</tt></a>').appendTo(rl),
        op = $('<span class="operations"> </span>').prependTo(rul);

        rul.hover(function(){
            $('#case-result .list-group-item:nth('+rul.index()+')').
                addClass("hover");

            routeHov(rul);
        }, function(){ 
            $('#case-result .list-group-item:nth('+rul.index()+')').
                removeClass("hover");
        
            op.html(' ')
        }).click(function(){ routeOp(rul); return false }).
            data('json', JSON.stringify({ 
                '_type': typ, 'pattern': pat, 'result': res 
            }));
        
        var di = $('<i class="fa fa-table"></i>'),
        dr = $('<li class="list-group-item">' + restxt + '</li>').
            appendTo("#case-result .list-group").prepend(di).
            hover(function(){ di.css({'visibility':"visible"}) },
                  function(){ di.css({'visibility':"hidden"}) });

        if (resovr) dr.popover(resovr);
        
        testBut.removeAttr("disabled").
            html('Run defined connection <i class="fa fa-chevron-right"></i>')

    },
    applyRoute = $("#route-editor .ac-apply").click(function(){
        var pat, ps = [], res = null, typ = rk.val();

        $("#param-pattern li").each(function(i,e){ 
            var p = $(e);
            ps.push({'_type':p.data('type'),'value':p.data('value')})
        });

        pat = {'expression':re.val(), 'parameters':ps};

        if (raiserr.is(":checked")) {
            res = {'error':rer.val()}
        } else if (typ == "update") {
            res = {'updateCount':parseInt($("#update-count").val())}
        } else {
            res = {'schema':[],'rows':[]};

            $("#result-data .res-row:first td:not(:first)").each(function(i,e){
                var cd = $(e), ct = cd.data('type'), cn = cd.data("name");

                res.schema.push({'_type':ct,'name':cn})
            });

            $("#result-data .res-row").each(function(i,e){
                var row = [];

                $("td:not(:first)", e).each(function(j,r){ 
                    row.push($(r).text())
                });

                res.rows.push(row)
            });

            $("#result-data tbody > tr:not(.res-row)").remove();
            $("#result-data .res-row").
                each(function(i,tr){ $("td:first", tr).remove() });

        }

        $("#case-rules .text-muted").remove();
        $("#case-result .text-muted").remove();

        rd.modal('hide');

        loadRoute(typ,pat,res)
    }),
    addUpEd = function() {
        var uc = $('<input type="number" min="0" id="update-count" class="form-control" />'),
        hasC = function() {
            if (isNum(uc)) applyRoute.removeAttr("disabled");
            else applyRoute.attr("disabled", "disabled")
        },
        ucr = $('<input type="radio" name="result-type" id="update-success"/>').on('keyup change', function() {
            var re = $(this);
            
            if (re.is(":checked")) {
                uc.removeAttr("disabled");
                hasC()
            } else uc.attr("disabled", "disabled")
        });

        $('<div class="input-group"></div>').append(uc.on('keyup change', hasC).click(function(){ ucr.trigger('click') })).prepend($('<label class="input-group-addon"> Update count</label>').prepend(ucr)).tooltip({'title':"Execution is successful, then count of updated rows is returned."}).appendTo(red.empty())
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
                var hn = $('<input type="text" class="form-control col-md-6 col-sm-12" id="col' + j + 'name" value="Column #'+j+'" />').
                    on('keyup change', function(){
                        var te = $(this);

                        if (!notEmpty(te)) {
                            te.addClass("has-error");
                            crb.attr("disabled", "disabled");
                            return false
                        } else {
                            te.removeClass("has-error");
                            crb.removeAttr("disabled");
                            return true
                        }
                    }).tooltip({'title':"Name of column #"+j}),
                hs = $('<select id="col' + j + 'type" class="form-control col-md-6 col-sm-12"></select>').tooltip({'title':"Type of column #"+j});

                $.each(pts, function(v, t) {
                    hs.append('<option value="'+v+'">'+t+'</option>')
                });

                gct.push(colDef(hn, hs));

                $('<div class="row col-def"></div>').
                    append(hn).append(hs).appendTo(nre)
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
                gcv = [], gci = function(n, ty, e) {
                    return function() {
                        var v = e.val();
                        e.val(null);
                        return {'name':n, '_type':ty, 'val':v}
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

                        $('<td>'+col.val+'</td>').appendTo(nr).
                            data("type", col._type).data("name", col.name)

                    }

                    nr.insertBefore(tbe).hover(function() {
                        ri.css({'display':"inline"})
                    }, function() { ri.css({'display':"none"}) }).
                        click(rmrow);

                    applyRoute.removeAttr("disabled");

                    return false
                };

                for (j = 1; j <= cc; j++) {
                    var cd = gct[j-1](), // column definition
                    cn = cd.name, ct = cd._type,
                    ce = $('<input type="text" class="form-control" />');

                    th.append('<td class="text-center">'+cn+'</td>');

                    ce.appendTo($('<td></td>').appendTo(tbe));

                    if (ct == "date") {
                        cvf.push(vdv(ce));
                        ce.attr("readonly", "readonly").
                            datepicker({'format':"yyyy-mm-dd"}).
                            on('changeDate', rv).
                            tooltip({'title':"Date for "+cn})

                    } else if (ct == "float") {
                        cvf.push(vnv(ce));
                        ce.on('keyup change', rav(isNum)).
                            on('keyup change', rv).
                            tooltip({'title':"Number for "+cn})

                    } else {
                        ce.on('keyup change', rv).
                            tooltip({'title':"Text for "+cn})
                    }

                    gcv.push(gci(cn, ct, ce))
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
        if (e.is(":checked")) rer.removeAttr("readonly")
        else rer.attr("readonly", "readonly")
    });

    moveRoute.click(function(){
        if (!moveRoute.hasClass("active")) {
            routeHov = mvRouteHov;
            routeOp = function(){}
        } else {
            routeHov = rmRouteHov;
            routeOp = rmRoute
        }
    });

    $('.has-tooltip').one('mouseover', rt).tooltip();

    $._loadRoutes = function(arr){
        if (arr.length > 0) {
            $("#case-rules .text-muted").remove();
            $("#case-result .text-muted").remove()
        }

        var i;
        for (i = 0; i < arr.length; i++) {
            loadRoute(arr[i]._type, arr[i].pattern, arr[i].result)
        }
    }
})(jQuery);
