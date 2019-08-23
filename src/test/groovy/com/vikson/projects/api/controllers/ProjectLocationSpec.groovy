package com.vikson.projects.api.controllers

import com.vikson.projects.model.OrganisationRootFolder
import com.vikson.projects.model.Project
import com.vikson.projects.model.ProjectFolder
import com.vikson.projects.repositories.ProjectRepository
import com.vikson.projects.repositories.OrganisationRootFolderRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.ZonedDateTime

@SpringBootTest
@ActiveProfiles(["dev"])
class ProjectLocationSpec extends Specification {

    @Autowired
    ProjectRepository projectRepository

    @Autowired
    OrganisationRootFolderRepository folderRepository


    def 'getProjectLocations'() {
        given:
        def organisationId = UUID.randomUUID()
        def folder = new ProjectFolder(UUID.randomUUID(), "Peter", organisationId, null)
        def project0 = new Project(UUID.randomUUID(), "Sarah", organisationId, folder, ZonedDateTime.now())
        def project0Adress = project0.getAddress()
        project0Adress.cc = "AT"
        project0Adress.zipCode = "1130"
        def project1 = new Project(UUID.randomUUID(), "Ruby", organisationId, folder, ZonedDateTime.now())
        def project1Adress = project1.getAddress()
        project1Adress.cc = "DE"
        project1Adress.zipCode = "1130"
        def project2 = new Project(UUID.randomUUID(), "Rosa", organisationId, folder, ZonedDateTime.now())
        def project2Adress = project2.getAddress()
        project2Adress.cc = "AT"
        project2Adress.zipCode = "1010"
        def project3 = new Project(UUID.randomUUID(), "Kaylen", organisationId, folder, ZonedDateTime.now())
        def project3Adress = project3.getAddress()
        project3Adress.cc = "AT"
        project3Adress.zipCode = "1010"
        folderRepository.saveAndFlush(new OrganisationRootFolder(organisationId, folder))
        projectRepository.saveAndFlush(project0)
        projectRepository.saveAndFlush(project1)
        projectRepository.saveAndFlush(project2)
        projectRepository.saveAndFlush(project3)

        when:
        def results = projectRepository.getLocationsDistinctByZipCode()

        then:
        results.numberOfElements == 3
    }
}
