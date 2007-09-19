declare namespace local = "http://www.foobar.org";
declare function local:convert($v as xs:decimal?) as xs:decimal?
{
  2.20371 * $v
};

for    $i in /site/open_auctions/open_auction
return local:convert($i/reserve/text())
