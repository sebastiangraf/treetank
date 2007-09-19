for    $p in /site/people/person
let    $a := for $t in /site/closed_auctions/closed_auction
             where $t/buyer/@person = $p/@id
             return $t
return <item person="{ $p/name/text() }">{ count($a) }</item>
