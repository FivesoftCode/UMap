package com.fivesoft.umap.object;

import com.fivesoft.umap.data.UMap;
import com.fivesoft.umap.template.MapTemplate;
import org.jetbrains.annotations.NotNull;

public abstract class UMapObject {

    private MapTemplate template;

    @NotNull
    protected MapTemplate createTemplate(){
        return null;
    }

    public final UMap asUMap(){
        return UMap.wrap(this);
    }

    @NotNull
    public final MapTemplate getTemplate(){
        synchronized(this){
            if(template == null){
                template = createTemplate();
            }
        }
        return template;
    }

}
