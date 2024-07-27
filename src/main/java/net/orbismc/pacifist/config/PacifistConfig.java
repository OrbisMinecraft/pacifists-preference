// Copyright © 2024. OrbisMC Contributors
// SPDX-License-Identifier: AGPL-3.0-only
package net.orbismc.pacifist.config;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.potion.PotionEffectType;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class PacifistConfig {
    public String placeholderPvpEnabled = "\uD83D\uDDE1";
    public String placeholderPvpDisabled = "";
    public boolean showParticles = true;
    public boolean showGlowing = false;

    public HashMap<Material, Double> blockedMaterials = new HashMap<>() {{
        put(Material.LAVA_BUCKET, 10.0);
        put(Material.FIRE, 6.0);
    }};

    public Set<String> blockedPotionEffects = new HashSet<>() {{
        add("SLOWNESS");
        add("MINING_FATIGUE");
        add("INSTANT_DAMAGE");
        add("NAUSEA");
        add("BLINDNESS");
        add("HUNGER");
        add("WEAKNESS");
        add("POISON");
        add("WITHER");
        add("LEVITATION");
        add("UNLUCK");
        add("BAD_OMEN");
        add("DARKNESS");
        add("TRIAL_OMEN");
        add("RAID_OMEN");
        add("INFESTED");
    }};

    public String blockPotionEffectRule = "any";

    /**
     * Loads the configuration from a file.
     *
     * @param file The file to load the configuration from.
     * @return The configuration loaded.
     * @throws FileNotFoundException If the file does not exist (duh.)
     */
    public static PacifistConfig load(final File file) throws FileNotFoundException {
        // ... I won't even bother to ask why this causes a ClassNotFound exception if
        // not using a custom Proxy to the Constructor
        final var type = new YAMLConstructorProxy(PacifistConfig.class);
        type.addClassInfo(Material.class);

        final var yml = new Yaml(type);
        return yml.load(new FileInputStream(file));
    }

    /**
     * Saves the configuration to a file.
     *
     * @param file The file to save the configuration to.
     * @throws IOException If writing to the file fails.
     */
    public static void save(final PacifistConfig config, final File file) throws IOException {
        // Why ...? Why SnakeYAML? Is this really what we should be using?
        final var repr = new Representer(new DumperOptions());
        repr.addClassTag(PacifistConfig.class, Tag.MAP);
        repr.addClassTag(Material.class, Tag.STR);

        var opt = new DumperOptions();
        opt.setIndent(2);
        opt.setCanonical(false);
        opt.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        opt.setExplicitEnd(false);
        opt.setExplicitStart(false);
        opt.setPrettyFlow(true);
        opt.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        final var yml = new Yaml(repr, opt);
        yml.dump(config, new FileWriter(file));
    }

    public Set<PotionEffectType> translatePotionEffectTypes() {
        return blockedPotionEffects.stream().map(v -> Registry.EFFECT.get(NamespacedKey.minecraft(v.toLowerCase()))).collect(Collectors.toCollection(HashSet::new));
    }

    /**
     * Hacky workaround to SnakeYAML's issue with ClassNotFound exceptions when using a regular
     * {@link Constructor}.
     */
    private static class YAMLConstructorProxy extends Constructor {
        private final HashMap<String, Class<?>> classMap = new HashMap<>();

        public YAMLConstructorProxy(final Class<?> theRoot) {
            super(theRoot, new LoaderOptions());
            addClassInfo(theRoot);
        }

        public void addClassInfo(final Class<?> c) {
            classMap.put(c.getName(), c);
        }

        /*
         * This is a modified version of the Constructor. Rather than using a class loader to
         * get external classes, they are already predefined above. This approach works similar to
         * the typeTags structure in the original constructor, except that class information is
         * pre-populated during initialization rather than runtime.
         *
         * @see org.yaml.snakeyaml.constructor.Constructor#getClassForNode(org.yaml.snakeyaml.nodes.Node)
         */
        protected Class<?> getClassForNode(final Node node) {
            String name = node.getTag().getClassName();
            Class<?> cl = classMap.get(name);
            if (cl == null)
                throw new YAMLException("Class not found: " + name);
            else
                return cl;
        }
    }
}