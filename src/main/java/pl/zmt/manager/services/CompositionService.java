package pl.zmt.manager.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.zmt.manager.entities.Composition;
import pl.zmt.manager.entities.Repo;
import pl.zmt.manager.entities.Set;
import pl.zmt.manager.repositories.CompositionRepository;
import pl.zmt.manager.trees.Tree;
import pl.zmt.manager.trees.TreeType;

import java.util.*;

@Service
public class CompositionService {

    @Autowired
    private CompositionRepository compositionRepository;
    @Autowired
    private SetService setService;
    @Autowired
    private RepoService repoService;

    public List<Repo> findAllBySetName(String name) {
        Set set = setService.findByName(name);
        Collection<Composition> compositions  = compositionRepository.findAllBySet(set.getId());

        List<Repo> repos = new ArrayList<Repo>();

        for (Composition c : compositions) {
            Optional<Repo> optional = repoService.findById(c.getRepository());
            optional.ifPresent(repos::add);
        }
        return repos;
    }

    public void deleteRepo(Integer id_set, Long id_repo) {
        Composition c = compositionRepository.findBySetAndRepository(id_set,id_repo);
        compositionRepository.delete(c);
    }

    public Collection<Repo> findAllUnusedRepo(List<Repo> used_repo) {
        Collection<Repo> all_repos = repoService.findAll();
        Collection<Repo> used_repos = used_repo;
        all_repos.removeAll(used_repos);
        return all_repos;
    }

    public void add(Integer id_set, Long id_repo) {
        compositionRepository.save(new Composition(id_set, id_repo));
    }

    public Tree returnTree(Collection<Set> sets) {
        Tree rootTree = new Tree("Sets");
        Tree parent = rootTree;
        for( Set s: sets) {
            Tree child = parent.addChild(new Tree(s.getName(), s.getDescription(), TreeType.SET));
            List<Repo> repos = findAllBySetName(s.getName());
            if(!repos.isEmpty()) {
                parent = child;
                for (Repo r : repos) {
                    Tree child1 = parent.addChild(new Tree(r.getIndexRepo(), r.getName(), TreeType.REPOSITORY));
                }
                parent = parent.getParent();
            }
            java.util.Set<Set> components = setService.findAllComponents(s.getName());
            if(!components.isEmpty()) {
                parent = child;
                for (Set c : components) {
                    Tree child2 = parent.addChild(new Tree(c.getName(), c.getDescription(), TreeType.SET));
                }
                parent = parent.getParent();
            }
        }
        return rootTree;
    }


}
