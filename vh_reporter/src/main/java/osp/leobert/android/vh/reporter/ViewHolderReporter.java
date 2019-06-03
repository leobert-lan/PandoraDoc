package osp.leobert.android.vh.reporter;

import com.google.auto.service.AutoService;

import net.steppschuh.markdowngenerator.image.Image;
import net.steppschuh.markdowngenerator.list.ListBuilder;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.list.UnorderedListItem;
import net.steppschuh.markdowngenerator.text.Text;
import net.steppschuh.markdowngenerator.text.code.Code;
import net.steppschuh.markdowngenerator.text.emphasis.BoldText;
import net.steppschuh.markdowngenerator.text.heading.Heading;
import net.steppschuh.markdowngenerator.text.quote.Quote;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import osp.leobert.android.reportprinter.spi.Model;
import osp.leobert.android.reportprinter.spi.ReporterExtension;
import osp.leobert.android.reportprinter.spi.Result;

@AutoService(ReporterExtension.class)
public class ViewHolderReporter implements ReporterExtension {

    public static class Foo {
        Model model;

        ViewHolder viewHolder;

        Foo(Model model, ViewHolder viewHolder) {
            this.model = model;
            this.viewHolder = viewHolder;
        }

        public static Foo create(Model model, ViewHolder viewHolder) {
            return new Foo(model, viewHolder);
        }
    }

    private final Map<String, List<Foo>> groupedByUsage = new LinkedHashMap<>();
    private final List<String> viewHolderDocBlocks = new ArrayList<>();
    private final String END = "\n";
    private final String RETURN = "\r\n";

    @Override
    public Set<String> applicableAnnotations() {
        return Collections.singleton(ViewHolder.class.getName());
    }

    @Override
    public Result generateReport(Map<String, List<Model>> previousData) {
        if (previousData == null)
            return null;

        List<Model> ViewHolderModels = previousData.get(ViewHolder.class.getName());
        if (ViewHolderModels == null || ViewHolderModels.isEmpty())
            return Result.newBuilder().handled(false).build();
        StringBuilder docBuilder = new StringBuilder();

        for (Model model : ViewHolderModels) {
            ViewHolder annotation = model.getElement().getAnnotation(ViewHolder.class);
            groupByUsage(model, annotation);
            generateDocBlock(model, annotation);
        }

        docBuilder.append(new Heading("ViewHolders used in MotorFans")).append(END);


        docBuilder.append(new BoldText(getDay())).append(END);

        docBuilder.append(new Heading("ViewHolder Groups", 2)).append(END);

        //All Groups
        docBuilder.append(new BoldText("All Groups:")).append(END).append(RETURN);
        UnorderedList<UnorderedListItem> allGroupsList = new UnorderedList<>();
        Set<String> groups = groupedByUsage.keySet();
        final List<UnorderedListItem> allGroupsListItems = new ArrayList<>();


        for (String group : groups) {
            allGroupsListItems.add(new UnorderedListItem(group));
        }
        allGroupsList.setItems(allGroupsListItems);
        docBuilder.append(allGroupsList).append("\r\n\r\n");


        //Each ViewHolder used in each group
        UnorderedList eachGroupsDetail;
        ListBuilder listBuilder = new ListBuilder();
        docBuilder.append(new BoldText("Usage Details")).append(END).append(RETURN);

        for (String group : groups) {
            listBuilder.append(group);
            final ListBuilder eachGroupBuilder = new ListBuilder();

            List<Foo> foos = groupedByUsage.get(group);
            for (Foo foo : foos) {
                eachGroupBuilder.append(wrapNavigation(getAlias(foo.model, foo.viewHolder)));
            }
            listBuilder.append(eachGroupBuilder);
        }
        eachGroupsDetail = listBuilder.build();
        docBuilder.append(eachGroupsDetail).append(new Text("\r\n\r\n"));


        docBuilder.append(new Heading("ViewHolder Detail", 2)).append(END).append(RETURN);

        for (String docBlock : viewHolderDocBlocks) {
            docBuilder.append(docBlock).append(RETURN);
        }

        return Result.newBuilder()
                .handled(true)
                .reportFileNamePrefix("ViewHolders")
                .fileExt("md")
                .reportContent(docBuilder.toString())
                .build();
    }

    private void groupByUsage(Model model, ViewHolder notation) {
        String[] usages = notation.usage();
        for (String usage : usages) {
            if (groupedByUsage.containsKey(usage)) {
                groupedByUsage.get(usage).add(Foo.create(model, notation));
            } else {
                List<Foo> list = new ArrayList<>();
                list.add(Foo.create(model, notation));
                groupedByUsage.put(usage, list);
            }
        }
    }

    /**
     * ### {ViewHolder name}
     * SupportVersion: `1` , `2` , ...
     * Used At:
     */
    private void generateDocBlock(Model model, ViewHolder notation) {
        String[] usages = notation.usage();
        int[] versions = notation.version();
        String[] pics = notation.pic();

        StringBuilder docBlockBuilder = new StringBuilder();

        //header
        String alias = getAlias(model, notation);
        docBlockBuilder.append(new Heading(wrapAnchor(alias), 3)).append(END);

        docBlockBuilder.append(new Quote("ClassPath: " + model.getName())).append(END);
        docBlockBuilder.append(RETURN);

        //support version
        docBlockBuilder.append(new Text("SupportVersion: "));
        for (int version : versions) {
            docBlockBuilder.append(new Code(new Text(String.valueOf(version))))
                    .append(new Text(" , "));
        }
        docBlockBuilder.append(END).append(RETURN);

        //Used At
        docBlockBuilder.append(new Text("Used At: "));
        for (String usage : usages) {
            docBlockBuilder.append(new BoldText(usage))
                    .append(new Text(" , "));
        }
        docBlockBuilder.append(END).append(RETURN);

        //Sample picture

        docBlockBuilder.append(new Text("Sample Picture: ")).append(RETURN);
        for (String pic : pics) {
            docBlockBuilder.append(new Image(pic)).append(RETURN);
        }
        docBlockBuilder.append(END).append(RETURN);

        docBlockBuilder.append(RETURN);

        viewHolderDocBlocks.add(docBlockBuilder.toString());
    }

    private String wrapNavigation(String column) {
        return "<a href=\"#" + column + "\">" + column + "</a>";
    }

    private String wrapAnchor(String column) {
        return "<a name=\"" + column + "\">" + column + "</a>";
    }

    private String getAlias(Model model, ViewHolder viewHolder) {

        String name = model.getName();
        if (name.contains(".")) {
            int index = name.lastIndexOf(".");
            name = name.substring(index + 1);
        }

        String alias = viewHolder.alias();
        if ("".equals(alias)) {
            return name;
        } else {
            if (alias.contains(name))
                return alias;
            return alias + " " + name;
        }
    }

    private String getDay() {
        Date date = new Date(System.currentTimeMillis());
        DateFormat timeStyle = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINESE);
        return timeStyle.format(date);
    }
}
