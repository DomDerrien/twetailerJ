/**
 * @author Dom Derrien
 * @link http://anothersocialeconomy.appspot.com/widget/sample.jsp for the details
 */
(function() {
    var aC = window.ase_config || { lg: 'en', referralId: '0' },
        lg = aC.lg || 'en',
        ht = aC.hashtag || 'core',
        dm = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'anothersocialeconomy.appspot.com/widget/', // '10.0.2.2:9999/widget/', // 'localhost:9999/widget/', //
        id = 'ase_widget',
        dc = document,
        bdl = {
            core: { fr: 'Réseau d\'achat locaux', en: 'Local Buying Network' },
            cardealer: { fr: 'Rechercher un véhicule d\'occasion', en: 'Find Local Pre-Owned Cars' }
        }
        installWidget = function(){
            var dv = dc.getElementById(id),
                txt = escape(window.getSelection ? window.getSelection() : dc.getSelection ? dc.getSelection() : dc.selection ? dc.selection.createRange().text : ''),
                src = dm + (aC.hashtag || 'ase') + '.jsp?criteria=' + txt,
                bd = aC.brand || bdl[ht][lg],
                ifr, p;
            for(p in aC) {
                if (p != 'brand' && p != 'lg') {
                    src += '&' + p + '=' + (escape('' + aC[p]));
                }
            }
            if (!document.all) try { bd = decodeURIComponent(escape(bd)); } catch(ex) {} // For Chrome
            src += '&brand=' + bd + '&lg=' + lg;
            if (dv) {
                ifr = dc.getElementById(id + 'Ifr');
                if (txt == '' || ifr.src.indexOf('criteria=' + txt + '&') != -1) {
                    dv.style.display = '';
                }
                else {
                    dv.parentNode.removeChild(dv);
                    dv = null;
                }
            }
            if (!dv) {
                var cl = lg == 'fr' ? 'Fermer [X]' : 'Close [X]',
                    cpc = '100%',
                    hd;
                dv = dc.createElement('div');
                dv.id = id;
                dv.setAttribute('class', 'ase_widget');
                dc.getElementsByTagName('body')[0].appendChild(dv);
                hd = dc.createElement('span');
                hd.innerHTML = '<a href="#" onclick="document.getElementById(\''+id+'\').style.display=\'none\';return false;">'+ cl + '<a>';
                dv.appendChild (hd);
                ifr = dc.createElement('iframe');
                ifr.id = id + 'Ifr';
                ifr.width = cpc;
                ifr.height = cpc;
                ifr.border = '0';
                ifr.frameBorder = '0';
                ifr.src = src;
                dv.appendChild(ifr);
            }
            return false;
        },
        installCSS = function() {
            var lk = dc.createElement('link'),
                hd = dc.getElementsByTagName('head')[0];
            lk.href = dm + 'widget-loader.css';
            lk.rel = 'stylesheet';
            lk.type = 'text/css';
            hd.appendChild(lk);
        },
        installTab = function() {
            var ac = dc.createElement('a'),
                dv = dc.createElement('div'),
                bd = aC.brand || bdl[ht][lg],
                hd = dc.getElementsByTagName('body')[0],
                cl, bi;
            ac.href = '#';
            ac.id = 'ase_floatingTab';
            ac.onclick = installWidget;
            if (!document.all) try { bd = decodeURIComponent(escape(bd)) } catch(ex) {} // For Chrome
            ac.appendChild(dc.createTextNode(bd));
            if (aC['color-brand']) {
                ac.style.color = aC['color-brand'];
            }
            dv.setAttribute('class', 'ase_floatingTab');
            dv.appendChild(ac);
            if (aC['border']) {
                dv.style.border = aC['border'];
            }
            cl = aC['background-color'];
            if (cl) {
                bi = cl.indexOf(' !');
                if (bi != -1) {
                    cl = cl.substring(0, bi);
                }
                else {
                    bi = cl.indexOf('!');
                    if (bi != -1) {
                        cl = cl.substring(0, bi);
                    }
                }
                dv.style.backgroundColor = cl;
            }
            hd.appendChild(dv);
            if (document.all) {
                dv.style.top = '100px';
            }
        };
    installCSS();
    if (aC.showWidget) {
        installWidget();
    }
    else {
        installTab();
    }
})();
