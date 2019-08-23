UPDATE public.plan_revisions SET image_width=image_height,image_height=image_width WHERE id IN
    (SELECT revision_id FROM (SELECT * ,ROW_NUMBER() OVER (PARTITION BY revision_id) AS row_number FROM
        (SELECT * FROM public.plan_levels AS x join public.plan_revisions AS y  on x.revision_id=y.id ORDER BY (tiles_x*tiles_y) DESC) AS levels
            WHERE type != 'Map' AND  tiles_x*256-image_width>256 AND ((tiles_x>tiles_y AND image_width<image_height) OR (tiles_x<tiles_y AND image_width>image_height)) ) as effected_revisions where row_number=1)