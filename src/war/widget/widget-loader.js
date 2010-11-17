/**
 * @author Dom Derrien
 */
(function() {
    var aC = ase_config || { lg: 'en', referralId: '0' },
        lg = aC.lg,
        ht = aC.hashtag || 'core',
        dm = '../../widget/', // 'https://anothersocialeconomy.appspot.com/widget/',
        id = 'ase_widget',
        dc = document,
        bd = {
            core: { fr: 'Réseau d\'achat locaux', en: 'Local Buying Network' },
            cardealer: { fr: 'Rechercher un véhicule d\'occasion', en: 'Find Local Pre-Owned Cars' }
        }
        installWidget = function(){
            var dv = dc.getElementById(id),
                txt = escape(window.getSelection ? window.getSelection() : dc.getSelection ? dc.getSelection() : dc.selection ? dc.selection.createRange().text : ''),
                src = dm + (aC.hashtag || 'ase') + '.jsp?criteria=' + txt,
                ifr, p;
            for(p in aC) {
                src += '&' + p + '=' + escape(aC[p]);
            }
            if (!aC.brand) {
                src += '&brand=' + escape(bd[ht][lg]);
            }
            console.log(src.replace(/\&/g, '\n'));
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
            var lk = dc.createElement('link');
            lk.href = dm + 'widget-loader.css';
            lk.rel = 'stylesheet';
            lk.type = 'text/css';
            dc.getElementsByTagName('head')[0].appendChild(lk);
        },
        installTab = function() {
            var ac = dc.createElement('a'),
                dv = dc.createElement('div');
            ac.href = '#';
            ac.id = 'ase_floatingTab';
            ac.onclick = installWidget;
            ac.appendChild(dc.createTextNode(aC.brand || bd[ht][lg]));
            if (aC['color-brand']) {
                ac.style.color = aC['color-brand'];
            }
            dv.setAttribute('class', 'ase_floatingTab');
            dv.appendChild(ac);
            if (aC['border']) {
                dv.style.border = aC['border'];
            }
            if (aC['background-color']) {
                dv.style.backgroundColor = aC['background-color'];
            }
            dc.getElementsByTagName('body')[0].appendChild(dv);
            if (document.all) {
                dv.style.top = '100px';
            }
        };
    installCSS();
    if (typeof window.ase_showWidget == 'undefined') {
        installTab();
    }
    else {
        installWidget();
    }
})();
