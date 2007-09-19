for    $b in /site/open_auctions/open_auction
return <increase>{ $b/bidder[1]/increase/text() }</increase>
