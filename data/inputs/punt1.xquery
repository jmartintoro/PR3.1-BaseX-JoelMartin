declare option output:method "xml";
declare option output:indent "yes";

<posts>{
  for $p in /posts/row[@PostTypeId='1']
  order by xs:integer($p/@ViewCount) descending
  return <article><title>{$p/@Title/string()}</title><viewCount>{$p/@ViewCount/string()}</viewCount></article>
}</posts>
