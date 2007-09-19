for $p in /site
let $c1 := count($p//description),
    $c2 := count($p//mail),
    $c3 := count($p//email),
    $sum := $c1 + $c2 + $c3
return $sum
