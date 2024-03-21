declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  for $p in /posts/row[@PostTypeId='1']
  order by xs:integer($p/@Score) descending
  return <article>
    <title>{$p/@Title/string()}</title>
    <body>{$p/@Body/string()}</body>
    <viewCount>{$p/@ViewCount/string()}</viewCount>
    <mostVotedResponse>{
      let $resp := /posts/row[@PostTypeId='2' and @ParentId=$p/@Id]
      let $topResp :=  $resp[@Score=max(@Score)]
      return
        <responseBody>{$topResp/@Body/string()}</responseBody>
    }</mostVotedResponse>
    <score>{$p/@Score/string()}</score>
    <tags>{$p/@Tags/string()}</tags>
  </article>
}</posts>
