package github.com.coderyw.model;


import groovy.transform.builder.Builder;

@Builder
public class ApifoxImportSwagger {
    public String input;
    public  Options options;
    @Builder
    public static class Options{
        //用于存储或匹配 API 接口的目标目录的 ID。如果未指定，目标目录将为 Root 目录。
        public int targetEndpointFolderId;
        // 用于存储或匹配数据模型的目标目录的 ID。如果未指定，目标目录将为 Root 目录。
        public int targetSchemaFolderId;
        //指定处理匹配的接口的行为。确定是否覆盖现有的接口，自动合并更改，跳过更改并保留现有的接口，或创建新的接口。
        //
        //枚举值:
        //OVERWRITE_EXISTING
        //AUTO_MERGE
        //KEEP_EXISTING
        //CREATE_NEW
        //默认值: OVERWRITE_EXISTING
        String endpointOverwriteBehavior;

        //指定处理匹配的数据模型的行为。确定是否覆盖现有的模式，自动合并更改，跳过更改并保留现有的模式，或创建新的模式。
        //
        //枚举值:
        //OVERWRITE_EXISTING
        //AUTO_MERGE
        //KEEP_EXISTING
        //CREATE_NEW
        //默认值:
        //OVERWRITE_EXISTING
        String schemaOverwriteBehavior;
        //在导入匹配的现有接口时，是否更新接口的目录 ID。如果希望随导入的接口一起更改目录 ID，则应将其设置为 true。
        boolean updateFolderOfChangedEndpoint;
        //是否将基础路径添加到接口的路径中，默认设置为 false。我们建议将其设置为 false，这样基础路径可以保留在“环境面板”中，而不是每个接口内部。如果希望在接口路径中添加路径前缀，则应将其设置为 true。
        boolean prependBasePath;

    }
}
