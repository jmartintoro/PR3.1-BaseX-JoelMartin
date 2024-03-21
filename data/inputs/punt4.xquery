declare option output:method "xml";
declare option output:indent "yes";

let $mostUsedTags := (
  for $t in /tags/row
  order by xs:integer($t/@Count) descending
  return $t/@TagName/string()
)[10]

let $postsWithTags := (
  for $post in /posts/row[@PostTypeId='1']
  where some $tag in $mostUsedTags satisfies contains($post/@Tags, $tag)
  order by xs:integer($post/@ViewCount) descending
  return $post
)[position() <= 100]

return
<posts>{
  for $post in $postsWithTags
  return
  <post>
    <title>{$post/@Title/string()}</title>
    <viewCount>{$post/@ViewCount/string()}</viewCount>
    <tags>{$post/@Tags/string()}</tags>
  </post>
}</posts>


