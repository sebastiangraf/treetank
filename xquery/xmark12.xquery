(: mistake in the XMark paper... last tag closing as /person  :)

for $p in /site/people/person
let $l := for $i in /site/open_auctions/open_auction/initial
          where $p/profile/@income > (5000 * $i/text())
          return $i
where  $p/profile/@income > 50000
return <items person="{ $p/profile/@income }">{ count($l) }</items>
