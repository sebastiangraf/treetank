for    $p in /site/people/person
let    $l := for $i in /site/open_auctions/open_auction/initial
             where $p/profile/@income > (5000 * $i/text())
             return $i
return <items name="{ $p/profile/@income }">{ count($l) }</items>
